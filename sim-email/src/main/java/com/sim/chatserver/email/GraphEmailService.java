package com.sim.chatserver.email;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * EmailService implementation backed by Microsoft Graph API.
 */
public class GraphEmailService implements EmailService {

    private static final String EMPTY = "";
    private static final String MSG_GRAPH_EMAIL_CONFIG_REQUIRED = "GraphEmailConfig is required";
    private static final String MSG_GRAPH_TOKEN_CLIENT_REQUIRED = "GraphTokenClient is required";
    private static final String MSG_GRAPH_MAIL_CLIENT_REQUIRED = "GraphMailClient is required";
    private static final String MSG_MARKDOWN_RENDERER_REQUIRED = "MarkdownRenderer is required";
    private static final String LOG_GRAPH_EMAIL_SENT_PREFIX = "Graph email sent successfully. sender=";
    private static final String LOG_GRAPH_EMAIL_SENT_SUBJECT_SEGMENT = ", subject=";
    private static final String LOG_GRAPH_EMAIL_SENT_TO_COUNT_SEGMENT = ", toCount=";
    private static final String MSG_GRAPH_EMAIL_CONFIG_INCOMPLETE =
            "Graph email config is incomplete. Required: tenantId, clientId, clientSecret, senderUser";
    private static final String MSG_EMAIL_MESSAGE_NULL = "EmailMessage is null";
    private static final String MSG_TO_RECIPIENT_REQUIRED = "At least one TO recipient is required";
    private static final String MSG_EMAIL_SUBJECT_REQUIRED = "Email subject is required";


    private static final Logger LOG = Logger.getLogger(GraphEmailService.class.getCanonicalName());

    private final GraphEmailConfig config;
    private final GraphTokenClient tokenClient;
    private final GraphMailClient mailClient;
    private final MarkdownRenderer markdownRenderer;

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    GraphEmailService(
            GraphEmailConfig config,
            GraphTokenClient tokenClient,
            GraphMailClient mailClient,
            MarkdownRenderer markdownRenderer
    ) {
        this.config = Objects.requireNonNull(config, MSG_GRAPH_EMAIL_CONFIG_REQUIRED);
        this.tokenClient = Objects.requireNonNull(tokenClient, MSG_GRAPH_TOKEN_CLIENT_REQUIRED);
        this.mailClient = Objects.requireNonNull(mailClient, MSG_GRAPH_MAIL_CLIENT_REQUIRED);
        this.markdownRenderer = Objects.requireNonNull(markdownRenderer, MSG_MARKDOWN_RENDERER_REQUIRED);
    }

    @Override
    public void send(EmailMessage message) {
        validateConfig();
        validateMessage(message);

        String token = tokenClient.getAccessToken();
        mailClient.sendMail(token, config, message, markdownRenderer);

        LOG.info(LOG_GRAPH_EMAIL_SENT_PREFIX + safe(config.senderUser())
            + LOG_GRAPH_EMAIL_SENT_SUBJECT_SEGMENT + safe(message.subject())
            + LOG_GRAPH_EMAIL_SENT_TO_COUNT_SEGMENT + size(message.to()));
    }

    private void validateConfig() {
        if (!config.isUsable()) {
            throw new IllegalArgumentException(MSG_GRAPH_EMAIL_CONFIG_INCOMPLETE);
        }
    }

    private void validateMessage(EmailMessage message) {
        if (message == null) {
            throw new IllegalArgumentException(MSG_EMAIL_MESSAGE_NULL);
        }

        if (message.to() == null || message.to().stream().noneMatch(v -> v != null && !v.isBlank())) {
            throw new IllegalArgumentException(MSG_TO_RECIPIENT_REQUIRED);
        }

        if (message.subject() == null || message.subject().isBlank()) {
            throw new IllegalArgumentException(MSG_EMAIL_SUBJECT_REQUIRED);
        }
    }

    private int size(java.util.List<String> list) {
        return list == null ? 0 : list.size();
    }

    private String safe(String v) {
        return v == null ? EMPTY : v;
    }
}