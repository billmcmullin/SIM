package com.sim.chatserver.web.admin;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.service.WorkspaceClient;
import com.sim.chatserver.service.WorkflowRuntime;
import com.sim.chatserver.startup.AppDataSourceHolder;
import jakarta.enterprise.inject.spi.CDI;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.sql.DataSource;
import com.sim.chatserver.service.FullAnalysisService;
import com.sim.chatserver.widget.WidgetStore;

final class FullAnalysisSourceStore {
    private static final String TEXT_SELECT_PROMPT_RESPONSE_TEXT_FROM = "SELECT prompt,response_text FROM \"";
    private static final String TEXT_WHERE_WIDGET_CHAT_ID = "\" WHERE widget_chat_id=?";
    private static final String TEXT_SELECT_TO_REGCLASS = "SELECT to_regclass(?)";
    private static final String TEXT_CONFIGURE_THE_SAVED_SERVER_API_KEY_AND_WORKSPACE_BEFORE_STARTING = "Configure the saved server, API key and workspace before starting Full Analysis.";
    private static final String TEXT_EMPTY_5 = "://";
    private static final String TEXT_HTTP = "http://";
    private static final String TEXT_HTTPS = "https://";
    private static final String TEXT_HTTPS_2 = "https";
    private static final String TEXT_HTTP_2 = "http";
    private static final String TEXT_FULL_ANALYSIS_REQUIRES_A_VALID_SAVED_SERVER_ORIGIN_WITHOUT_CREDE = "Full Analysis requires a valid saved server origin without credentials or a path.";
    private static final String TEXT_FALSE = "false";
    private static final String TEXT_SIM_WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH = "sim.widget.sync.require.https.with.auth";
    private static final String TEXT_WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH = "WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH";
    private static final String TEXT_HTTPS_IS_REQUIRED_FOR_THE_CONFIGURED_API_KEY = "HTTPS is required for the configured API key.";
    private static final String TEXT_A_Z0_9 = "[^a-z0-9_-]";
    private static final String TEXT_EMPTY_6 = "-";
    private static final String TEXT_API_V1_WORKSPACE = "/api/v1/workspace/";
    private static final String TEXT_CHAT = "/chat";
    private static final String TEXT_UNABLE_TO_LOAD_THE_SAVED_ANALYSIS_TARGET = "Unable to load the saved analysis target.";

    private static final String TEXT_SELECT_WIDGET_CHAT_ID_COALESCE_OCTET_LENGTH_PROMPT_0_BIGINT_COAL = "SELECT widget_chat_id,"
                                + "COALESCE(octet_length(prompt),0)::bigint + COALESCE(octet_length(response_text),0)::bigint AS source_bytes"
                                + " FROM \"";
    private static final String TEXT_WHERE_CAST_CREATED_AT_AS_TIMESTAMP_AND_CAST_CREATED_AT_AS_TIMEST = "\" WHERE CAST(created_at AS TIMESTAMP)>=?"
                                + " AND CAST(created_at AS TIMESTAMP)<? ORDER BY CAST(created_at AS TIMESTAMP),widget_chat_id";
    private static final String TEXT_SOURCE_BYTES = "source_bytes";
    private static final String TEXT_SNAPSHOT_EXCEEDS_THE_EXPLICIT_8_MI_B_5000_CHAT_SAFETY_CAP_NO_CHA = "Snapshot exceeds the explicit 8 MiB/5000-chat safety cap. No chats were sent; narrow the selection.";
    private static final String TEXT_WIDGET_CHAT_ID = "widget_chat_id";
    private static final String TEXT_SOURCE_CHAT_HAS_NO_STABLE_ID_SNAPSHOT_CANNOT_BE_MARKED_COMPLETE = "Source chat has no stable ID; snapshot cannot be marked complete.";
    private static final String TEXT_SOURCE_ROW_DISAPPEARED_FROM_THE_REPEATABLE_READ_SNAPSHOT = "Source row disappeared from the repeatable-read snapshot.";
    private static final String TEXT_PROMPT = "prompt";
    private static final String TEXT_RESPONSE_TEXT = "response_text";
    private static final String TEXT_EMPTY = "/";
    private static final String TEXT_EMPTY_2 = "";
    private static final String TEXT_DUPLICATE_SOURCE_CHAT_ID_SNAPSHOT_CANNOT_BE_MARKED_COMPLETE = "Duplicate source chat ID; snapshot cannot be marked complete.";
    private static final String TEXT_UNABLE_TO_SNAPSHOT_FULL_CHAT_TEXT_NO_INFERENCE_WAS_STARTED = "Unable to snapshot full chat text. No inference was started.";
    private static final String TEXT_EMPTY_3 = "\"";
    private static final String TEXT_A_ZA_Z0_9 = "[^A-Za-z0-9_]";
    private static final String TEXT_EMPTY_4 = "_";
    private static final String TEXT_WIDGET = "widget";
    private static final String TEXT_W = "w_";

    private final DataSource dataSource;

    FullAnalysisSourceStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    List<FullAnalysisService.Chat> snapshot(LocalDate day, Instant requestedAt) {
        List<FullAnalysisService.Chat> chats = new ArrayList<>();
        try {
            var tables = new LinkedHashSet<String>();
            for (var widget : WidgetStore.list(null)) {
                if (widget != null && widget.getWidgetId() != null && !widget.getWidgetId().isBlank()) {
                    tables.add(tableName(widget.getWidgetId()));
                }
            }
            LocalDateTime upper = day.plusDays(1).atStartOfDay();
            LocalDateTime cutoff = requestedAt.atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (cutoff.isBefore(upper)) {
                upper = cutoff;
            }
            long bytes = 0;
            try (Connection connection = dataSource.getConnection()) {
                connection.setReadOnly(true);
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connection.setAutoCommit(false);
                try {
                    for (String table : tables) {
                        if (!exists(connection, table)) {
                            continue;
                        }
                        String sql = TEXT_SELECT_WIDGET_CHAT_ID_COALESCE_OCTET_LENGTH_PROMPT_0_BIGINT_COAL + table + TEXT_WHERE_CAST_CREATED_AT_AS_TIMESTAMP_AND_CAST_CREATED_AT_AS_TIMEST;
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setFetchSize(100);
                            statement.setTimestamp(1, Timestamp.valueOf(day.atStartOfDay()));
                            statement.setTimestamp(2, Timestamp.valueOf(upper));
                            try (ResultSet result = statement.executeQuery()) {
                                while (result.next()) {
                                    bytes += result.getLong(TEXT_SOURCE_BYTES);
                                    if (bytes > 8L * 1024 * 1024 || chats.size() >= 5000) {
                                        throw new IllegalStateException(TEXT_SNAPSHOT_EXCEEDS_THE_EXPLICIT_8_MI_B_5000_CHAT_SAFETY_CAP_NO_CHA);
                                    }
                                    String id = result.getString(TEXT_WIDGET_CHAT_ID);
                                    if (id == null || id.isBlank()) {
                                        throw new IllegalStateException(TEXT_SOURCE_CHAT_HAS_NO_STABLE_ID_SNAPSHOT_CANNOT_BE_MARKED_COMPLETE);
                                    }
                                    try (PreparedStatement fullText = connection.prepareStatement(
                                            TEXT_SELECT_PROMPT_RESPONSE_TEXT_FROM + table + TEXT_WHERE_WIDGET_CHAT_ID)) {
                                        fullText.setString(1, id);
                                        try (ResultSet full = fullText.executeQuery()) {
                                            if (!full.next()) {
                                                throw new IllegalStateException(TEXT_SOURCE_ROW_DISAPPEARED_FROM_THE_REPEATABLE_READ_SNAPSHOT);
                                            }
                                            String question = full.getString(TEXT_PROMPT);
                                            String answer = full.getString(TEXT_RESPONSE_TEXT);
                                            chats.add(new FullAnalysisService.Chat(table + TEXT_EMPTY + id,
                                                    question == null ? TEXT_EMPTY_2 : question, answer == null ? TEXT_EMPTY_2 : answer));
                                            if (full.next()) {
                                                throw new IllegalStateException(TEXT_DUPLICATE_SOURCE_CHAT_ID_SNAPSHOT_CANNOT_BE_MARKED_COMPLETE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    connection.rollback();
                }
            }
            return chats;
        } catch (SQLException error) {
            throw new IllegalStateException(TEXT_UNABLE_TO_SNAPSHOT_FULL_CHAT_TEXT_NO_INFERENCE_WAS_STARTED, error);
        }
    }

    private boolean exists(Connection connection, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(TEXT_SELECT_TO_REGCLASS)) {
            statement.setString(1, TEXT_EMPTY_3 + table + TEXT_EMPTY_3);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getString(1) != null;
            }
        }
    }

    static FullAnalysisService createService(FullAnalysisService.TargetResolver target) {
        var records = WorkflowRuntime.records();
        var dataSource = CDI.current().select(AppDataSourceHolder.class).get().getDataSource();
        WorkspaceClient client = new WorkspaceClient(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(15)).build(), 0, Duration.ofSeconds(90));
        return new FullAnalysisService(records, client, new FullAnalysisSourceStore(dataSource)::snapshot, target);
    }

    static FullAnalysisService.Target target() {
        try {
            var config = EncryptedDbConfigStore.load();
            if (config == null || config.getServerHost() == null || config.getApiKey() == null
                    || config.getApiKey().isBlank() || config.getWorkspaceName() == null || config.getWorkspaceName().isBlank()) {
                throw new IllegalStateException(TEXT_CONFIGURE_THE_SAVED_SERVER_API_KEY_AND_WORKSPACE_BEFORE_STARTING);
            }
            String host = config.getServerHost().strip();
            int port = config.getServerPort();
            URI base = URI.create(host.contains(TEXT_EMPTY_5) ? host : (port == 80 ? TEXT_HTTP : TEXT_HTTPS) + host);
            if (base.getHost() == null || base.getRawUserInfo() != null || base.getRawQuery() != null || base.getRawFragment() != null
                    || !(TEXT_HTTPS_2.equalsIgnoreCase(base.getScheme()) || TEXT_HTTP_2.equalsIgnoreCase(base.getScheme()))
                    || (base.getPath() != null && !base.getPath().isEmpty() && !TEXT_EMPTY.equals(base.getPath()))) {
                throw new IllegalStateException(TEXT_FULL_ANALYSIS_REQUIRES_A_VALID_SAVED_SERVER_ORIGIN_WITHOUT_CREDE);
            }
            if (!TEXT_HTTPS_2.equalsIgnoreCase(base.getScheme())
                    && !TEXT_FALSE.equalsIgnoreCase(System.getProperty(TEXT_SIM_WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH,
                        System.getenv().getOrDefault(TEXT_WIDGET_SYNC_REQUIRE_HTTPS_WITH_AUTH, com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE)))) {
                throw new IllegalStateException(TEXT_HTTPS_IS_REQUIRED_FOR_THE_CONFIGURED_API_KEY);
            }
            int effectivePort = base.getPort() >= 0 ? base.getPort() : port;
            URI origin = new URI(base.getScheme(), null, base.getHost(), effectivePort > 0 ? effectivePort : -1, null, null, null);
            String slug = config.getWorkspaceName().trim().toLowerCase(Locale.ROOT).replaceAll(TEXT_A_Z0_9, TEXT_EMPTY_6);
            return new FullAnalysisService.Target(origin + TEXT_API_V1_WORKSPACE
                    + URLEncoder.encode(slug, StandardCharsets.UTF_8) + TEXT_CHAT, config.getApiKey());
        } catch (SQLException | java.net.URISyntaxException error) {
            throw new IllegalStateException(TEXT_UNABLE_TO_LOAD_THE_SAVED_ANALYSIS_TARGET, error);
        }
    }

    private String tableName(String widgetId) {
        String table = widgetId.trim().replaceAll(TEXT_A_ZA_Z0_9, TEXT_EMPTY_4);
        if (table.isEmpty()) {
            table = TEXT_WIDGET;
        }
        if (!Character.isLetter(table.charAt(0))) {
            table = TEXT_W + table;
        }
        return new String(table.toCharArray(), 0, Math.min(60, table.length()));
    }
}