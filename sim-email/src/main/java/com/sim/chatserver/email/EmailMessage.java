package com.sim.chatserver.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final String MSG_SUBJECT_REQUIRED = "subject is required";
    private static final String MSG_RECIPIENT_REQUIRED = "At least one recipient is required";
    private static final String MSG_BODY_REQUIRED = "At least one body (text/html/markdown) is required";

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

    private static EmailMessageBuilder builder() {
        return new EmailMessageBuilder();
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
        EmailMessageBuilder builder = builder()
                .subject(subject)
                .textBody(textBody)
                .htmlBody(htmlBody)
                .markdownBody(markdownBody);

        if (from != null) {
            builder = builder.from(from);
        }
        if (to != null) {
            builder = builder.to(to);
        }
        if (cc != null) {
            builder = builder.cc(cc);
        }
        if (bcc != null) {
            builder = builder.bcc(bcc);
        }
        if (attachments != null) {
            builder = builder.attachments(attachments);
        }

        return builder.build();
    }

    List<String> allRecipientsReadOnly() {
        List<String> all = new ArrayList<>(to);
        all.addAll(cc);
        all.addAll(bcc);
        return Collections.unmodifiableList(all);
    }
}

