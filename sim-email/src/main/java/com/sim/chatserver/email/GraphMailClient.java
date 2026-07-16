package com.sim.chatserver.email;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sends mail through Microsoft Graph API.
 */
public class GraphMailClient {

    private static final Logger LOG = Logger.getLogger(GraphMailClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    void sendMail(String accessToken, GraphEmailConfig config, EmailMessage message, MarkdownRenderer markdownRenderer) {
        HttpsURLConnection conn = null;
        try {
            String sender = config.senderUser().trim();
            String endpoint = "https://graph.microsoft.com/v1.0/users/" + sender + "/sendMail";

            URL url = URI.create(endpoint).toURL();
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            String html = firstNonBlank(message.htmlBody(), markdownRenderer.toHtml(message.markdownBody()));
            String text = message.textBody();
            String contentType = (html != null && !html.isBlank()) ? "HTML" : "Text";
            String content = (html != null && !html.isBlank()) ? html : (text == null ? "" : text);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("contentType", contentType);
            body.put("content", content);

            Map<String, Object> graphMessage = new LinkedHashMap<>();
            graphMessage.put("subject", nvl(message.subject()));
            graphMessage.put("body", body);
            graphMessage.put("toRecipients", recipients(message.to()));
            graphMessage.put("ccRecipients", recipients(message.cc()));
            graphMessage.put("bccRecipients", recipients(message.bcc()));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", graphMessage);
            payload.put("saveToSentItems", Boolean.TRUE);

            byte[] json = MAPPER.writeValueAsBytes(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }

            int status = conn.getResponseCode();
            // Graph sendMail commonly returns 202 Accepted
            if (status != 202 && (status < 200 || status >= 300)) {
                String err = (conn.getErrorStream() == null)
                        ? ""
                        : new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                throw new EmailException(
                        "Graph sendMail failed. HTTP " + status + " body=" + err,
                        new RuntimeException("graph_send_http_" + status)
                );
            }
        } catch (EmailException e) {
            throw e;
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Graph mail send failed", e);
            throw new EmailException("Graph mail send failed", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private List<Map<String, Object>> recipients(List<String> emails) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (emails == null) {
            return result;
        }

        for (String e : emails) {
            if (e == null || e.isBlank()) {
                continue;
            }

            Map<String, Object> emailAddress = new LinkedHashMap<>();
            emailAddress.put("address", e.trim());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("emailAddress", emailAddress);

            result.add(recipient);
        }
        return result;
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

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
