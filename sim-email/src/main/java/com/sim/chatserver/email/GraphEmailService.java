package com.sim.chatserver.email;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * EmailService implementation backed by Microsoft Graph API.
 */
public class GraphEmailService implements EmailService {

    private static final Logger LOG = Logger.getLogger(GraphEmailService.class.getName());

    private final GraphEmailConfig config;
    private final GraphTokenClient tokenClient;
    private final GraphMailClient mailClient;
    private final MarkdownRenderer markdownRenderer;

    public GraphEmailService(
            GraphEmailConfig config,
            GraphTokenClient tokenClient,
            GraphMailClient mailClient,
            MarkdownRenderer markdownRenderer
    ) {
        this.config = Objects.requireNonNull(config, "GraphEmailConfig is required");
        this.tokenClient = Objects.requireNonNull(tokenClient, "GraphTokenClient is required");
        this.mailClient = Objects.requireNonNull(mailClient, "GraphMailClient is required");
        this.markdownRenderer = Objects.requireNonNull(markdownRenderer, "MarkdownRenderer is required");
    }

    @Override
    public void send(EmailMessage message) {
        validateConfig();
        validateMessage(message);

        try {
            String token = tokenClient.getAccessToken();
            mailClient.sendMail(token, config, message, markdownRenderer);

            LOG.info("Graph email sent successfully. sender=" + safe(config.senderUser())
                    + ", subject=" + safe(message.subject())
                    + ", toCount=" + size(message.to()));
        } catch (Exception e) {
            if (e instanceof EmailException ee) {
                throw ee;
            }
            throw new EmailException("Failed to send email via Microsoft Graph", e);
        }
    }

    private void validateConfig() {
        if (!config.isUsable()) {
            throw new IllegalArgumentException(
                    "Graph email config is incomplete. Required: tenantId, clientId, clientSecret, senderUser");
        }
    }

    private void validateMessage(EmailMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("EmailMessage is null");
        }

        if (message.to() == null || message.to().stream().noneMatch(v -> v != null && !v.isBlank())) {
            throw new IllegalArgumentException("At least one TO recipient is required");
        }

        if (message.subject() == null || message.subject().isBlank()) {
            throw new IllegalArgumentException("Email subject is required");
        }
    }

    private int size(java.util.List<String> list) {
        return list == null ? 0 : list.size();
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
