package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.SQLException;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;

@WebServlet(name = "TestAwsConnectionServlet", urlPatterns = {"/admin/test-aws-connection"})
public class TestAwsConnectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestAwsConnectionServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                return;
            }

            String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
            if (!"ADMIN".equalsIgnoreCase(role)) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
                return;
            }

            String awsRegion = ServletRequestParamUtil.firstParam(req, "awsRegion", 128, true, true);
            String awsInstanceId = ServletRequestParamUtil.firstParam(req, "awsInstanceId", 128, true, true);
            String awsAccessKeyId = ServletRequestParamUtil.firstParam(req, "awsAccessKeyId", 1024, true, true);
            String awsSecretAccessKey = ServletRequestParamUtil.firstParam(req, "awsSecretAccessKey", 4096, true, true);
            String requestId = UUID.randomUUID().toString();

            boolean requestProvidedAccessKeyId = !isBlank(awsAccessKeyId);
            boolean requestProvidedSecretAccessKey = !isBlank(awsSecretAccessKey);

            ServerConfig storedConfig = null;
            try {
                storedConfig = EncryptedDbConfigStore.load();
            } catch (SQLException | RuntimeException e) {
                log.log(Level.FINE, "Unable to load stored AWS configuration for test", e);
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

            String credentialSource = credentialSource(
                    requestProvidedAccessKeyId,
                    requestProvidedSecretAccessKey,
                    !isBlank(awsAccessKeyId),
                    !isBlank(awsSecretAccessKey));

            log.log(Level.INFO,
                    "aws-ec2-test requestId={0} credentialSource={1} requestAccessKeyIdProvided={2} requestSecretProvided={3}",
                    new Object[]{
                        requestId,
                        credentialSource,
                        Boolean.toString(requestProvidedAccessKeyId),
                        Boolean.toString(requestProvidedSecretAccessKey)
                    });

            if (isBlank(awsRegion) || isBlank(awsInstanceId) || isBlank(awsAccessKeyId) || isBlank(awsSecretAccessKey)) {
                JsonObject payload = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "AWS region, instance ID, access key ID, and secret access key are required.")
                        .add("requestId", requestId)
                        .add("credentialSource", credentialSource)
                        .add("errorCategory", "missing-input")
                        .build();
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, payload);
                return;
            }

            try {
                String instanceState = describeInstanceState(
                        awsRegion.trim(),
                        awsAccessKeyId.trim(),
                        awsSecretAccessKey.trim(),
                        awsInstanceId.trim());

                JsonObject payload = Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("message", "AWS connection successful.")
                        .add("instanceState", instanceState)
                    .add("requestId", requestId)
                    .add("credentialSource", credentialSource)
                        .build();
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, payload);
            } catch (IllegalStateException ex) {
                JsonObject payload = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", ex.getMessage() == null ? "AWS test request was invalid." : ex.getMessage())
                    .add("requestId", requestId)
                    .add("credentialSource", credentialSource)
                    .add("errorCategory", "instance-not-found")
                    .build();
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, payload);
            } catch (Ec2Exception ex) {
                log.log(Level.WARNING, "AWS EC2 test failed", ex);
                String message = ex.awsErrorDetails() != null
                        ? ex.awsErrorDetails().errorMessage()
                        : "AWS EC2 request failed.";
                String errorCode = ex.awsErrorDetails() != null && ex.awsErrorDetails().errorCode() != null
                    ? ex.awsErrorDetails().errorCode()
                    : "unknown";

                JsonObject payload = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", message)
                    .add("requestId", requestId)
                    .add("credentialSource", credentialSource)
                    .add("errorCategory", classifyAwsError(errorCode))
                    .add("errorCode", errorCode)
                    .build();
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY, payload);
            } catch (RuntimeException ex) {
                log.log(Level.WARNING, "AWS EC2 test failed", ex);
                JsonObject payload = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "AWS EC2 connection test failed.")
                    .add("requestId", requestId)
                    .add("credentialSource", credentialSource)
                    .add("errorCategory", "transport-or-runtime")
                    .build();
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_BAD_GATEWAY, payload);
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName())
                    .log(Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (IOException ioe) {
                    Logger.getLogger(getClass().getName())
                            .log(Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    String describeInstanceState(String region,
            String accessKeyId,
            String secretAccessKey,
            String instanceId) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()) {

            DescribeInstancesResponse response = ec2.describeInstances(
                    DescribeInstancesRequest.builder().instanceIds(instanceId).build());

            for (var reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if (instanceId.equals(instance.instanceId())) {
                        return instance.state() == null ? "unknown" : instance.state().nameAsString();
                    }
                }
            }
        }

        throw new IllegalStateException("EC2 instance was not found in the configured region.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String credentialSource(boolean requestProvidedAccessKeyId,
            boolean requestProvidedSecretAccessKey,
            boolean effectiveAccessKeyAvailable,
            boolean effectiveSecretAvailable) {
        boolean requestProvidedBoth = requestProvidedAccessKeyId && requestProvidedSecretAccessKey;
        if (requestProvidedBoth) {
            return "form";
        }

        boolean effectiveBoth = effectiveAccessKeyAvailable && effectiveSecretAvailable;
        if (!requestProvidedAccessKeyId && !requestProvidedSecretAccessKey && effectiveBoth) {
            return "stored";
        }

        if (effectiveBoth) {
            return "mixed";
        }

        return "missing";
    }

    private String classifyAwsError(String errorCode) {
        if (errorCode == null) {
            return "aws-service";
        }
        String code = errorCode.trim();
        if ("InvalidClientTokenId".equalsIgnoreCase(code)
                || "AuthFailure".equalsIgnoreCase(code)
                || "SignatureDoesNotMatch".equalsIgnoreCase(code)
                || "UnrecognizedClientException".equalsIgnoreCase(code)) {
            return "credentials-or-signing";
        }
        if ("UnauthorizedOperation".equalsIgnoreCase(code)
                || "AccessDenied".equalsIgnoreCase(code)
                || "AccessDeniedException".equalsIgnoreCase(code)) {
            return "permissions";
        }
        if ("InvalidInstanceID.NotFound".equalsIgnoreCase(code)) {
            return "instance-not-found";
        }
        if ("RequestLimitExceeded".equalsIgnoreCase(code)
                || "Throttling".equalsIgnoreCase(code)
                || "ThrottlingException".equalsIgnoreCase(code)) {
            return "throttling";
        }
        return "aws-service";
    }
}
