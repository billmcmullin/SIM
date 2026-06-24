package com.sim.chatserver.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    private EmailMessage(Builder b) {
        this.from = b.from;
        this.to = List.copyOf(b.to);
        this.cc = List.copyOf(b.cc);
        this.bcc = List.copyOf(b.bcc);
        this.subject = b.subject;
        this.textBody = b.textBody;
        this.htmlBody = b.htmlBody;
        this.markdownBody = b.markdownBody;
        this.attachments = List.copyOf(b.attachments);
    }

    public String from() {
        return from;
    }

    public List<String> to() {
        return to;
    }

    public List<String> cc() {
        return cc;
    }

    public List<String> bcc() {
        return bcc;
    }

    public String subject() {
        return subject;
    }

    public String textBody() {
        return textBody;
    }

    public String htmlBody() {
        return htmlBody;
    }

    public String markdownBody() {
        return markdownBody;
    }

    public List<EmailAttachment> attachments() {
        return attachments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String from;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject;
        private String textBody;
        private String htmlBody;
        private String markdownBody;
        private final List<EmailAttachment> attachments = new ArrayList<>();

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String recipient) {
            this.to.add(recipient);
            return this;
        }

        public Builder to(List<String> recipients) {
            this.to.addAll(recipients);
            return this;
        }

        public Builder cc(String recipient) {
            this.cc.add(recipient);
            return this;
        }

        public Builder cc(List<String> recipients) {
            this.cc.addAll(recipients);
            return this;
        }

        public Builder bcc(String recipient) {
            this.bcc.add(recipient);
            return this;
        }

        public Builder bcc(List<String> recipients) {
            this.bcc.addAll(recipients);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder textBody(String textBody) {
            this.textBody = textBody;
            return this;
        }

        public Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public Builder markdownBody(String markdownBody) {
            this.markdownBody = markdownBody;
            return this;
        }

        public Builder attachment(EmailAttachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<EmailAttachment> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        public EmailMessage build() {
            Objects.requireNonNull(subject, "subject is required");
            if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
                throw new IllegalArgumentException("At least one recipient is required");
            }
            if (isBlank(textBody) && isBlank(htmlBody) && isBlank(markdownBody)) {
                throw new IllegalArgumentException("At least one body (text/html/markdown) is required");
            }
            return new EmailMessage(this);
        }

        private boolean isBlank(String s) {
            return s == null || s.isBlank();
        }
    }

    public List<String> allRecipientsReadOnly() {
        List<String> all = new ArrayList<>(to);
        all.addAll(cc);
        all.addAll(bcc);
        return Collections.unmodifiableList(all);
    }
}
