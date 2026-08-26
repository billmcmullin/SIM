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

    private static final Logger LOG = Logger.getLogger(SmtpEmailService.class.getName());

    private final EmailConfig config;
    private final MarkdownRenderer markdownRenderer;

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
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
            throw new IllegalArgumentException("No from address provided (message.from or config.defaultFrom)");
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
            mimeMessage.setSubject(message.subject(), "UTF-8");

            MimeMultipart root = buildContent(message);
            mimeMessage.setContent(root);

            Transport.send(mimeMessage);
            LOG.info(() -> "Email sent successfully. subject=" + safe(message.subject()) + ", toCount=" + size(message.to()));
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, "SMTP send failed: " + e.getMessage(), e);
            throw new EmailException("Failed to send email", e);
        }
    }

    private void validateConfig() {
        if (isBlank(config.host())) {
            throw new IllegalArgumentException("SMTP host missing");
        }
        if (config.port() <= 0) {
            throw new IllegalArgumentException("SMTP port invalid: " + config.port());
        }

        if (config.auth()) {
            if (isBlank(config.username())) {
                throw new IllegalArgumentException("SMTP username missing");
            }
            if (isBlank(config.password())) {
                throw new IllegalArgumentException("SMTP password missing");
            }
        }
    }

    private void validateMessage(EmailMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("EmailMessage is null");
        }
        if (isBlank(message.subject())) {
            throw new IllegalArgumentException("Email subject is required");
        }

        List<String> to = message.to();
        if (to == null || to.stream().noneMatch(r -> r != null && !r.isBlank())) {
            throw new IllegalArgumentException("At least one TO recipient is required");
        }
    }

    private Properties buildProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));
        props.put("mail.smtp.auth", String.valueOf(config.auth()));

        // Office 365 recommended for smtp.office365.com:587
        props.put("mail.smtp.starttls.enable", String.valueOf(config.startTls()));
        props.put("mail.smtp.starttls.required", String.valueOf(config.startTls()));
        props.put("mail.smtp.ssl.enable", String.valueOf(config.ssl()));

        // timeouts
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

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
            MimeMultipart alternative = new MimeMultipart("alternative");

            if (text != null && !text.isBlank()) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(text, "UTF-8");
                alternative.addBodyPart(textPart);
            }

            if (html != null && !html.isBlank()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(html, "text/html; charset=UTF-8");
                alternative.addBodyPart(htmlPart);
            }

            if (alternative.getCount() == 0) {
                MimeBodyPart fallback = new MimeBodyPart();
                fallback.setText("", "UTF-8");
                alternative.addBodyPart(fallback);
            }

            bodyPart.setContent(alternative);

            MimeMultipart mixed = new MimeMultipart("mixed");
            mixed.addBodyPart(bodyPart);

            message.forEachAttachment(attachment -> {
                if (attachment != null) {
                    addAttachmentPart(mixed, attachment);
                }
            });

            return mixed;
        } catch (MessagingException | IllegalArgumentException e) {
            throw new EmailException("Failed to build email content", e);
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
            throw new EmailException("Failed to add attachment", e);
        }
    }

    private void logSmtpConfig(Properties props, String from) {
        LOG.info(()
                -> "SMTP config: host=" + props.getProperty("mail.smtp.host")
                + ", port=" + props.getProperty("mail.smtp.port")
                + ", auth=" + props.getProperty("mail.smtp.auth")
                + ", starttls=" + props.getProperty("mail.smtp.starttls.enable")
                + ", ssl=" + props.getProperty("mail.smtp.ssl.enable")
                + ", username=" + safe(config.username())
                + ", from=" + safe(from));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private int size(List<String> list) {
        return list == null ? 0 : list.size();
    }

    private String safe(String s) {
        return s == null ? "<null>" : s;
    }
}
