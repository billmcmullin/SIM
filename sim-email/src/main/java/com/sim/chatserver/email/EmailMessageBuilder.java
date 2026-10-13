package com.sim.chatserver.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class EmailMessageBuilder {

    private static final String MSG_SUBJECT_REQUIRED = "subject is required";
    private static final String MSG_RECIPIENT_REQUIRED = "At least one recipient is required";
    private static final String MSG_BODY_REQUIRED = "At least one body (text/html/markdown) is required";

    private String from;
    private final List<String> to = new ArrayList<>();
    private final List<String> cc = new ArrayList<>();
    private final List<String> bcc = new ArrayList<>();
    private String subject;
    private String textBody;
    private String htmlBody;
    private String markdownBody;
    private final List<EmailAttachment> attachments = new ArrayList<>();

    EmailMessageBuilder from(String from) {
        this.from = from;
        return this;
    }

    EmailMessageBuilder addTo(String recipient) {
        return to(recipient);
    }

    private EmailMessageBuilder to(String recipient) {
        this.to.add(recipient);
        return this;
    }

    EmailMessageBuilder to(List<String> recipients) {
        this.to.addAll(recipients);
        return this;
    }

    EmailMessageBuilder addCc(String recipient) {
        return cc(recipient);
    }

    private EmailMessageBuilder cc(String recipient) {
        this.cc.add(recipient);
        return this;
    }

    EmailMessageBuilder cc(List<String> recipients) {
        this.cc.addAll(recipients);
        return this;
    }

    EmailMessageBuilder addBcc(String recipient) {
        return bcc(recipient);
    }

    private EmailMessageBuilder bcc(String recipient) {
        this.bcc.add(recipient);
        return this;
    }

    EmailMessageBuilder bcc(List<String> recipients) {
        this.bcc.addAll(recipients);
        return this;
    }

    EmailMessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    EmailMessageBuilder textBody(String textBody) {
        this.textBody = textBody;
        return this;
    }

    EmailMessageBuilder htmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
        return this;
    }

    EmailMessageBuilder markdownBody(String markdownBody) {
        this.markdownBody = markdownBody;
        return this;
    }

    private EmailMessageBuilder attachment(EmailAttachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    EmailMessageBuilder addAttachment(EmailAttachment attachment) {
        return attachment(attachment);
    }

    EmailMessageBuilder attachments(List<EmailAttachment> attachments) {
        this.attachments.addAll(attachments);
        return this;
    }

    EmailMessage build() {
        subject = Objects.requireNonNull(subject, MSG_SUBJECT_REQUIRED);
        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
            throw new IllegalArgumentException(MSG_RECIPIENT_REQUIRED);
        }
        if (isBlank(textBody) && isBlank(htmlBody) && isBlank(markdownBody)) {
            throw new IllegalArgumentException(MSG_BODY_REQUIRED);
        }
        return new EmailMessage(
                from,
                to,
                cc,
                bcc,
                subject,
                textBody,
                htmlBody,
                markdownBody,
                attachments);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
