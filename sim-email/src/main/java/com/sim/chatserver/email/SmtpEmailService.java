package com.sim.chatserver.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.ByteArrayInputStream;
import java.util.Properties;

public class SmtpEmailService implements EmailService {

    private final EmailConfig config;
    private final MarkdownRenderer markdownRenderer;

    public SmtpEmailService(EmailConfig config, MarkdownRenderer markdownRenderer) {
        this.config = config;
        this.markdownRenderer = markdownRenderer;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            Session session = Session.getInstance(buildProperties(), buildAuthenticator());

            MimeMessage mimeMessage = new MimeMessage(session);
            String from = (message.from() != null && !message.from().isBlank())
                    ? message.from()
                    : config.defaultFrom();

            if (from == null || from.isBlank()) {
                throw new IllegalArgumentException("No from address provided (message.from or config.defaultFrom)");
            }

            mimeMessage.setFrom(new InternetAddress(from));
            addRecipients(mimeMessage, Message.RecipientType.TO, message.to());
            addRecipients(mimeMessage, Message.RecipientType.CC, message.cc());
            addRecipients(mimeMessage, Message.RecipientType.BCC, message.bcc());
            mimeMessage.setSubject(message.subject());

            MimeMultipart root = buildContent(message);
            mimeMessage.setContent(root);

            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email", e);
        }
    }

    private Properties buildProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));
        props.put("mail.smtp.auth", String.valueOf(config.auth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.startTls()));
        props.put("mail.smtp.ssl.enable", String.valueOf(config.ssl()));
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

    private void addRecipients(MimeMessage msg, Message.RecipientType type, java.util.List<String> recipients)
            throws MessagingException {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        for (String r : recipients) {
            if (r != null && !r.isBlank()) {
                msg.addRecipient(type, new InternetAddress(r));
            }
        }
    }

    private MimeMultipart buildContent(EmailMessage message) {
        try {
            String html = firstNonBlank(message.htmlBody(), markdownRenderer.toHtml(message.markdownBody()));
            String text = message.textBody();

            MimeBodyPart bodyPart = new MimeBodyPart();
            MimeMultipart alternative = new MimeMultipart("alternative");

            if (text != null && !text.isBlank()) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(text);
                alternative.addBodyPart(textPart);
            }

            if (html != null && !html.isBlank()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(html, "text/html; charset=UTF-8");
                alternative.addBodyPart(htmlPart);
            }

            bodyPart.setContent(alternative);

            MimeMultipart mixed = new MimeMultipart("mixed");
            mixed.addBodyPart(bodyPart);

            for (EmailAttachment a : message.attachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(a.fileName());
                attachmentPart.setContent(new ByteArrayInputStream(a.content()).readAllBytes(), a.contentType());
                mixed.addBodyPart(attachmentPart);
            }

            return mixed;
        } catch (Exception e) {
            throw new EmailException("Failed to build email content", e);
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
