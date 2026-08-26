package com.sim.chatserver.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class EmailMessage {

    private final String from;
    private final List<String> to;
    private final List<String> cc;
    private final List<String> bcc;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private final String markdownBody;
    private final List<EmailAttachment> attachments;

    EmailMessage(
            String from,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String subject,
            String textBody,
            String htmlBody,
            String markdownBody,
            List<EmailAttachment> attachments) {
        this.from = from;
        this.to = List.copyOf(to);
        this.cc = List.copyOf(cc);
        this.bcc = List.copyOf(bcc);
        this.subject = subject;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
        this.markdownBody = markdownBody;
        this.attachments = List.copyOf(attachments);
    }

    String from() {
        return from;
    }

    List<String> to() {
        return to;
    }

    List<String> cc() {
        return cc;
    }

    List<String> bcc() {
        return bcc;
    }

    String subject() {
        return subject;
    }

    String textBody() {
        return textBody;
    }

    String htmlBody() {
        return htmlBody;
    }

    String markdownBody() {
        return markdownBody;
    }

    void forEachAttachment(Consumer<EmailAttachment> consumer) {
        if (consumer == null) {
            return;
        }
        attachments.forEach(consumer);
    }

    static Builder builder() {
        return new Builder();
    }

    public static EmailMessage create(
            String from,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String subject,
            String textBody,
            String htmlBody,
            String markdownBody,
            List<EmailAttachment> attachments) {
        Builder builder = builder()
                .subject(subject)
                .textBody(textBody)
                .htmlBody(htmlBody)
                .markdownBody(markdownBody);

        if (from != null) {
            builder.from(from);
        }
        if (to != null) {
            builder.to(to);
        }
        if (cc != null) {
            builder.cc(cc);
        }
        if (bcc != null) {
            builder.bcc(bcc);
        }
        if (attachments != null) {
            builder.attachments(attachments);
        }

        return builder.build();
    }

    static final class Builder {

        private String from;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject;
        private String textBody;
        private String htmlBody;
        private String markdownBody;
        private final List<EmailAttachment> attachments = new ArrayList<>();

        Builder from(String from) {
            this.from = from;
            return this;
        }

        Builder addTo(String recipient) {
            return to(recipient);
        }

        Builder to(String recipient) {
            this.to.add(recipient);
            return this;
        }

        Builder to(List<String> recipients) {
            this.to.addAll(recipients);
            return this;
        }

        Builder addCc(String recipient) {
            return cc(recipient);
        }

        Builder cc(String recipient) {
            this.cc.add(recipient);
            return this;
        }

        Builder cc(List<String> recipients) {
            this.cc.addAll(recipients);
            return this;
        }

        Builder addBcc(String recipient) {
            return bcc(recipient);
        }

        Builder bcc(String recipient) {
            this.bcc.add(recipient);
            return this;
        }

        Builder bcc(List<String> recipients) {
            this.bcc.addAll(recipients);
            return this;
        }

        Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        Builder textBody(String textBody) {
            this.textBody = textBody;
            return this;
        }

        Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        Builder markdownBody(String markdownBody) {
            this.markdownBody = markdownBody;
            return this;
        }

        Builder attachment(EmailAttachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        Builder addAttachment(EmailAttachment attachment) {
            return attachment(attachment);
        }

        private Builder attachments(List<EmailAttachment> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        EmailMessage build() {
            Objects.requireNonNull(subject, "subject is required");
            if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
                throw new IllegalArgumentException("At least one recipient is required");
            }
            if (isBlank(textBody) && isBlank(htmlBody) && isBlank(markdownBody)) {
                throw new IllegalArgumentException("At least one body (text/html/markdown) is required");
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

        private boolean isBlank(String s) {
            return s == null || s.isBlank();
        }
    }

    List<String> allRecipientsReadOnly() {
        List<String> all = new ArrayList<>(to);
        all.addAll(cc);
        all.addAll(bcc);
        return Collections.unmodifiableList(all);
    }
}
