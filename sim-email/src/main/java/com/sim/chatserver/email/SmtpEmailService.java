package com.sim.chatserver.email;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class SmtpEmailService implements EmailService {

    private static final String EMPTY = "";
    private static final String UTF8 = "UTF-8";
    private static final String SMTP_TIMEOUT_MS = "10000";
    private static final String SMTP_PROP_HOST = "mail.smtp.host";
    private static final String SMTP_PROP_PORT = "mail.smtp.port";
    private static final String SMTP_PROP_AUTH = "mail.smtp.auth";
    private static final String SMTP_PROP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String SMTP_PROP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    private static final String SMTP_PROP_SSL_ENABLE = "mail.smtp.ssl.enable";
    private static final String SMTP_PROP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
    private static final String SMTP_PROP_TIMEOUT = "mail.smtp.timeout";
    private static final String SMTP_PROP_WRITE_TIMEOUT = "mail.smtp.writetimeout";
    private static final String MIME_MULTIPART_ALTERNATIVE = "alternative";
    private static final String MIME_MULTIPART_MIXED = "mixed";
    private static final String MIME_TEXT_HTML_UTF8 = "text/html;charset=UTF-8";
    private static final String NULL_LABEL = "<null>";
    private static final String MSG_NO_FROM_ADDRESS = "No from address provided (message.from or config.defaultFrom)";
    private static final String LOG_EMAIL_SENT_SUBJECT_PREFIX = "Email sent successfully. subject=";
    private static final String LOG_EMAIL_SENT_TO_COUNT_PREFIX = ", toCount=";
    private static final String LOG_SMTP_SEND_FAILED_PREFIX = "SMTP send failed: ";
    private static final String MSG_SEND_EMAIL_FAILED = "Failed to send email";
    private static final String MSG_SMTP_HOST_MISSING = "SMTP host missing";
    private static final String MSG_SMTP_PORT_INVALID_PREFIX = "SMTP port invalid: ";
    private static final String MSG_SMTP_USERNAME_MISSING = "SMTP username missing";
    private static final String MSG_SMTP_PASSWORD_MISSING = "SMTP password missing";
    private static final String MSG_EMAIL_MESSAGE_NULL = "EmailMessage is null";
    private static final String MSG_EMAIL_SUBJECT_REQUIRED = "Email subject is required";
    private static final String MSG_TO_RECIPIENT_REQUIRED = "At least one TO recipient is required";
    private static final String MSG_BUILD_EMAIL_CONTENT_FAILED = "Failed to build email content";
    private static final String MSG_ADD_ATTACHMENT_FAILED = "Failed to add attachment";
    private static final String LOG_SMTP_CONFIG_HOST_PREFIX = "SMTP config: host=";
    private static final String LOG_SMTP_CONFIG_PORT_PREFIX = ", port=";
    private static final String LOG_SMTP_CONFIG_AUTH_PREFIX = ", auth=";
    private static final String LOG_SMTP_CONFIG_STARTTLS_PREFIX = ", starttls=";
    private static final String LOG_SMTP_CONFIG_SSL_PREFIX = ", ssl=";
    private static final String LOG_SMTP_CONFIG_USERNAME_PREFIX = ", username=";
    private static final String LOG_SMTP_CONFIG_FROM_PREFIX = ", from=";


    private static final Logger LOG = Logger.getLogger(SmtpEmailService.class.getCanonicalName());

    private final EmailConfig config;
    private final MarkdownRenderer markdownRenderer;

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    SmtpEmailService(EmailConfig config, MarkdownRenderer markdownRenderer) {
        this.config = config;
        this.markdownRenderer = markdownRenderer;
    }

    @Override
    public void send(EmailMessage message) {
        validateConfig();
        validateMessage(message);

        String from = (message.from() != null && !message.from().isBlank())
                ? message.from()
                : config.defaultFrom();

        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException(MSG_NO_FROM_ADDRESS);
        }

        Properties props = buildProperties();
        logSmtpConfig(props, from);

        try {
            Session session = Session.getInstance(props, buildAuthenticator());
            // Enable temporarily if you want verbose SMTP protocol logs:
            // session.setDebug(true);

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from));
            addRecipients(mimeMessage, Message.RecipientType.TO, message.to());
            addRecipients(mimeMessage, Message.RecipientType.CC, message.cc());
            addRecipients(mimeMessage, Message.RecipientType.BCC, message.bcc());
            mimeMessage.setSubject(message.subject(), UTF8);

            MimeMultipart root = buildContent(message);
            mimeMessage.setContent(root);

            Transport.send(mimeMessage);
            LOG.info(() -> LOG_EMAIL_SENT_SUBJECT_PREFIX + safe(message.subject()) + LOG_EMAIL_SENT_TO_COUNT_PREFIX + size(message.to()));
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, LOG_SMTP_SEND_FAILED_PREFIX + e.getMessage(), e);
            throw new EmailException(MSG_SEND_EMAIL_FAILED, e);
        }
    }

    private void validateConfig() {
        if (isBlank(config.host())) {
            throw new IllegalArgumentException(MSG_SMTP_HOST_MISSING);
        }
        if (config.port() <= 0) {
            throw new IllegalArgumentException(MSG_SMTP_PORT_INVALID_PREFIX + config.port());
        }

        if (config.auth()) {
            if (isBlank(config.username())) {
                throw new IllegalArgumentException(MSG_SMTP_USERNAME_MISSING);
            }
            if (isBlank(config.password())) {
                throw new IllegalArgumentException(MSG_SMTP_PASSWORD_MISSING);
            }
        }
    }

    private void validateMessage(EmailMessage message) {
        if (message == null) {
            throw new IllegalArgumentException(MSG_EMAIL_MESSAGE_NULL);
        }
        if (isBlank(message.subject())) {
            throw new IllegalArgumentException(MSG_EMAIL_SUBJECT_REQUIRED);
        }

        List<String> to = message.to();
        if (to == null || to.stream().noneMatch(r -> r != null && !r.isBlank())) {
            throw new IllegalArgumentException(MSG_TO_RECIPIENT_REQUIRED);
        }
    }

    private Properties buildProperties() {
        Properties props = new Properties();
        props.setProperty(SMTP_PROP_HOST, config.host());
        props.setProperty(SMTP_PROP_PORT, String.valueOf(config.port()));
        props.setProperty(SMTP_PROP_AUTH, String.valueOf(config.auth()));

        // Office 365 recommended for smtp.office365.com:587
        props.setProperty(SMTP_PROP_STARTTLS_ENABLE, String.valueOf(config.startTls()));
        props.setProperty(SMTP_PROP_STARTTLS_REQUIRED, String.valueOf(config.startTls()));
        props.setProperty(SMTP_PROP_SSL_ENABLE, String.valueOf(config.ssl()));

        // timeouts
        props.setProperty(SMTP_PROP_CONNECTION_TIMEOUT, SMTP_TIMEOUT_MS);
        props.setProperty(SMTP_PROP_TIMEOUT, SMTP_TIMEOUT_MS);
        props.setProperty(SMTP_PROP_WRITE_TIMEOUT, SMTP_TIMEOUT_MS);

        return props;
    }

    private Authenticator buildAuthenticator() {
        if (!config.auth()) {
            return null;
        }
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username(), config.password());
            }
        };
    }

    private void addRecipients(MimeMessage msg, Message.RecipientType type, List<String> recipients)
            throws MessagingException {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        for (String r : recipients) {
            if (r != null && !r.isBlank()) {
                msg.addRecipient(type, new InternetAddress(r.trim()));
            }
        }
    }

    private MimeMultipart buildContent(EmailMessage message) {
        try {
            String html = message.htmlBody();
            if (isBlank(html)) {
                String markdown = message.markdownBody();
                if (!isBlank(markdown)) {
                    html = markdownRenderer.toHtml(markdown);
                }
            }
            String text = message.textBody();

            MimeBodyPart bodyPart = new MimeBodyPart();
            MimeMultipart alternative = new MimeMultipart(MIME_MULTIPART_ALTERNATIVE);

            if (text != null && !text.isBlank()) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(text, UTF8);
                alternative.addBodyPart(textPart);
            }

            if (html != null && !html.isBlank()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(html, MIME_TEXT_HTML_UTF8);
                alternative.addBodyPart(htmlPart);
            }

            if (alternative.getCount() == 0) {
                MimeBodyPart fallback = new MimeBodyPart();
                fallback.setText(EMPTY, UTF8);
                alternative.addBodyPart(fallback);
            }

            bodyPart.setContent(alternative);

            MimeMultipart mixed = new MimeMultipart(MIME_MULTIPART_MIXED);
            mixed.addBodyPart(bodyPart);

            message.forEachAttachment(attachment -> {
                if (attachment != null) {
                    addAttachmentPart(mixed, attachment);
                }
            });

            return mixed;
        } catch (MessagingException | IllegalArgumentException e) {
            throw new EmailException(MSG_BUILD_EMAIL_CONTENT_FAILED, e);
        }
    }

    private void addAttachmentPart(MimeMultipart mixed, EmailAttachment attachment) {
        byte[] content = attachment.content();
        if (content == null) {
            return;
        }

        try {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName(attachment.fileName());
            attachmentPart.setContent(content, attachment.contentType());
            mixed.addBodyPart(attachmentPart);
        } catch (MessagingException e) {
            throw new EmailException(MSG_ADD_ATTACHMENT_FAILED, e);
        }
    }

    private void logSmtpConfig(Properties props, String from) {
        LOG.info(()
            -> LOG_SMTP_CONFIG_HOST_PREFIX + props.getProperty(SMTP_PROP_HOST)
            + LOG_SMTP_CONFIG_PORT_PREFIX + props.getProperty(SMTP_PROP_PORT)
            + LOG_SMTP_CONFIG_AUTH_PREFIX + props.getProperty(SMTP_PROP_AUTH)
            + LOG_SMTP_CONFIG_STARTTLS_PREFIX + props.getProperty(SMTP_PROP_STARTTLS_ENABLE)
            + LOG_SMTP_CONFIG_SSL_PREFIX + props.getProperty(SMTP_PROP_SSL_ENABLE)
                + LOG_SMTP_CONFIG_USERNAME_PREFIX + safe(config.username())
                + LOG_SMTP_CONFIG_FROM_PREFIX + safe(from));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private int size(List<String> list) {
        return list == null ? 0 : list.size();
    }

    private String safe(String s) {
        return s == null ? NULL_LABEL : s;
    }
}