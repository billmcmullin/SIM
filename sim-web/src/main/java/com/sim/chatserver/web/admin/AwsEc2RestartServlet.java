package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.RebootInstancesRequest;

@WebServlet(name = "AwsEc2RestartServlet", urlPatterns = {"/admin/aws/restart-ec2"})
public class AwsEc2RestartServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AwsEc2RestartServlet.class.getName());
    private static final int MAX_IP_LENGTH = 64;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String requestId = UUID.randomUUID().toString();
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                auditRestart(requestId, "(anonymous)", "", "", "", "blocked", HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required.", Level.WARNING);
                writeErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                return;
            }

            String username = session.getAttribute("user") == null ? "(unknown)" : session.getAttribute("user").toString();

            String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
            if (!"ADMIN".equalsIgnoreCase(role)) {
                auditRestart(requestId, username, "", "", "", "blocked", HttpServletResponse.SC_FORBIDDEN,
                        "Admin role required.", Level.WARNING);
                writeErrorSafe(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
                return;
            }

            String clientIp = resolveClientIpForAudit(req);

            String awsRegion = ServletRequestParamUtil.firstParam(req, "awsRegion", 128, true, true);
            String awsInstanceId = ServletRequestParamUtil.firstParam(req, "awsInstanceId", 128, true, true);
            String awsAccessKeyId = ServletRequestParamUtil.firstParam(req, "awsAccessKeyId", 1024, true, true);
            String awsSecretAccessKey = ServletRequestParamUtil.firstParam(req, "awsSecretAccessKey", 4096, true, true);
            String restartConfirmed = ServletRequestParamUtil.firstParam(req, "restartConfirmed", 16, true, true);

            ServerConfig storedConfig = null;
            try {
                storedConfig = EncryptedDbConfigStore.load();
            } catch (SQLException | IllegalStateException e) {
                log.log(Level.FINE, "Unable to load stored AWS configuration for restart", e);
            }

            if (isBlank(awsRegion) && storedConfig != null) {
                awsRegion = storedConfig.getAwsRegion();
            }
            if (isBlank(awsInstanceId) && storedConfig != null) {
                awsInstanceId = storedConfig.getAwsInstanceId();
            }
            if (isBlank(awsAccessKeyId) && storedConfig != null) {
                awsAccessKeyId = storedConfig.getAwsAccessKeyId();
            }
            if (isBlank(awsSecretAccessKey) && storedConfig != null) {
                awsSecretAccessKey = storedConfig.getAwsSecretAccessKey();
            }

            if (isBlank(awsRegion) || isBlank(awsInstanceId) || isBlank(awsAccessKeyId) || isBlank(awsSecretAccessKey)) {
                auditRestart(requestId, username, clientIp, safe(awsRegion), safe(awsInstanceId), "blocked",
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Required AWS configuration values are missing.", Level.INFO);
                writeErrorSafe(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "AWS region, instance ID, access key ID, and secret access key are required.");
                return;
            }

            String normalizedRegion = awsRegion.trim();
            String normalizedInstanceId = awsInstanceId.trim();
            String normalizedAccessKeyId = awsAccessKeyId.trim();
            String normalizedSecretAccessKey = awsSecretAccessKey.trim();
            String normalizedRestartConfirmed = restartConfirmed == null ? "" : restartConfirmed.trim();

            if (!"true".equalsIgnoreCase(normalizedRestartConfirmed)) {
                auditRestart(requestId, username, clientIp, normalizedRegion, normalizedInstanceId, "blocked",
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Restart confirmation prompt was not accepted.", Level.INFO);
                writeErrorSafe(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Restart was not confirmed.");
                return;
            }

            try {
                rebootEc2Instance(
                        normalizedRegion,
                        normalizedAccessKeyId,
                        normalizedSecretAccessKey,
                        normalizedInstanceId);

                auditRestart(requestId, username, clientIp, normalizedRegion, normalizedInstanceId, "success",
                    HttpServletResponse.SC_OK,
                    "EC2 reboot request submitted.", Level.INFO);

                JsonObject payload = Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("message", "EC2 reboot request submitted.")
                    .add("instanceId", normalizedInstanceId)
                        .build();
                writeJsonSafe(resp, HttpServletResponse.SC_OK, payload);
            } catch (AwsServiceException ex) {
                log.log(Level.WARNING, "AWS EC2 reboot failed", ex);
                String message = ex.awsErrorDetails() != null
                        ? ex.awsErrorDetails().errorMessage()
                        : "AWS EC2 reboot request failed.";
                auditRestart(requestId, username, clientIp, normalizedRegion, normalizedInstanceId, "failed",
                    HttpServletResponse.SC_BAD_GATEWAY,
                    message, Level.WARNING);
                writeErrorSafe(resp, HttpServletResponse.SC_BAD_GATEWAY, message);
            } catch (SdkClientException ex) {
                log.log(Level.WARNING, "AWS EC2 reboot failed", ex);
                auditRestart(requestId, username, clientIp, normalizedRegion, normalizedInstanceId, "failed",
                    HttpServletResponse.SC_BAD_GATEWAY,
                    "AWS EC2 reboot request failed.", Level.WARNING);
                writeErrorSafe(resp, HttpServletResponse.SC_BAD_GATEWAY, "AWS EC2 reboot request failed.");
            }
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            Logger.getLogger(getClass().getName())
                    .log(Level.WARNING, "Unhandled exception in doPost", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (IOException ioe) {
                    Logger.getLogger(getClass().getName())
                            .log(Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    final void rebootEc2Instance(String region,
            String accessKeyId,
            String secretAccessKey,
            String instanceId) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build()) {
            try {
                ec2.rebootInstances(RebootInstancesRequest.builder().instanceIds(instanceId).build());
            } catch (AwsServiceException | SdkClientException ex) {
                throw ex;
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void auditRestart(String requestId,
            String username,
            String clientIp,
            String region,
            String instanceId,
            String outcome,
            int status,
            String message,
            Level level) {
        log.log(
                level,
                "aws-ec2-restart requestId={0} user={1} ip={2} region={3} instanceId={4} outcome={5} status={6} message=\"{7}\"",
                new Object[]{
                    safe(requestId),
                    safe(username),
                    safe(clientIp),
                    safe(region),
                    safe(instanceId),
                    safe(outcome),
                    Integer.toString(status),
                    scrub(message)
                }
        );
    }

    private String resolveClientIpForAudit(HttpServletRequest req) {
        return "";
    }

    private void writeErrorSafe(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write JSON error response", ex);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, safe(message));
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Unable to write fallback error response", ioe);
                }
            }
        }
    }

    private void writeJsonSafe(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write JSON success response", ex);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response payload.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Unable to write fallback error response", ioe);
                }
            }
        }
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "(unknown)";
        }
        return scrub(value);
    }

    private String scrub(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replaceAll("[\\p{Cntrl}]", " ")
                .trim();
    }
}
