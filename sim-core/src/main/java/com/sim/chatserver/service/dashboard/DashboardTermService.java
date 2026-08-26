package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

/**
 * Term-focused dashboard logic (term summary + chart payload).
 */
@Dependent
public class DashboardTermService {

    private static final Logger LOG = Logger.getLogger(DashboardTermService.class.getName());

    public static final String OTHER_PARASOFT_LABEL = "Other Parasoft Match";

    private final TermsStore termsStore;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @Inject
    DashboardTermService(TermsStore termsStore) {
        this.termsStore = termsStore;
    }

    final List<TermDefinition> loadAllTerms() {
        try {
            List<TermDefinition> terms = termsStore.listAll();
            return terms == null ? List.of() : terms;
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.FINE, "loadAllTerms fallback to empty list", e);
            return List.of();
        }
    }

    /**
     * Existing behavior: all-time (no date filter).
     */
    final TermSummary buildTermSummary(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        return buildTermSummary(conn, widgets, terms, null, null);
    }

    public final TermSummary buildTermSummaryForDashboard(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        return buildTermSummary(conn, widgets, terms);
    }

    /**
     * New behavior: optional date range filter (inclusive start/end by day). If
     * either start or end is null, falls back to all-time behavior.
     */
    final TermSummary buildTermSummary(
            Connection conn,
            List<WidgetEntry> widgets,
            List<TermDefinition> terms,
            LocalDate rangeStartInclusive,
            LocalDate rangeEndInclusive
    ) throws SQLException {
        TermSummary summary = new TermSummary();
        if (widgets == null || widgets.isEmpty() || terms == null) {
            return summary;
        }

        List<TermDefinition> activeTerms = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();

        for (TermDefinition term : terms) {
            if (term == null || term.isSystemFlag()) {
                continue;
            }
            activeTerms.add(term);
            compiledPatterns.add(TermMatcher.buildStrictPattern(term));
            summary.ensureTerm(term.getName());
        }

        summary.ensureTerm(OTHER_PARASOFT_LABEL);

        Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

        final boolean useDateRange = rangeStartInclusive != null && rangeEndInclusive != null;
        final Timestamp startTs = useDateRange ? Timestamp.valueOf(rangeStartInclusive.atStartOfDay()) : null;
        final Timestamp endExclusiveTs = useDateRange ? Timestamp.valueOf(rangeEndInclusive.plusDays(1).atStartOfDay()) : null;

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }

            String widgetId = widget.getWidgetId();
            String tableName = DashboardDbUtil.sanitizeWidgetTableName(widgetId);
            if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                continue;
            }
            processWidgetRows(
                    conn,
                    tableName,
                    useDateRange,
                    startTs,
                    endExclusiveTs,
                    activeTerms,
                    compiledPatterns,
                    summary,
                    widgetId
            );
        }

        return summary;
    }

    private void processWidgetRows(
            Connection conn,
            String tableName,
            boolean useDateRange,
            Timestamp startTs,
            Timestamp endExclusiveTs,
            List<TermDefinition> activeTerms,
            List<Pattern> compiledPatterns,
            TermSummary summary,
            String widgetId
    ) throws SQLException {
        String sql;
        if (useDateRange) {
            sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                    + DashboardDbUtil.quoteIdentifier(tableName)
                    + " WHERE created_at >= ? AND created_at < ?";
        } else {
            sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                    + DashboardDbUtil.quoteIdentifier(tableName);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (useDateRange) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endExclusiveTs);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chatId = rs.getString("widget_chat_id");
                    if (chatId == null) {
                        chatId = "";
                    }

                    String prompt = rs.getString("prompt");
                    if (prompt == null) {
                        prompt = "";
                    }

                    String response = rs.getString("response_text");
                    Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                    String sessionId = rs.getString("session_id");

                    String snapshotTerm = resolveSnapshotTerm(prompt, activeTerms, compiledPatterns);
                    TermChatSnapshot snapshot = new TermChatSnapshot(
                            snapshotTerm,
                            widgetId,
                            chatId,
                            prompt,
                            response,
                            createdAt,
                            sessionId
                    );
                    summary.recordMatch(snapshotTerm, snapshot);
                }
            }
        }
    }

    private String resolveSnapshotTerm(String prompt, List<TermDefinition> activeTerms, List<Pattern> compiledPatterns) {
        final String sanitizedPrompt = TextSanitizer.sanitizeForMatching(prompt);
        TermDefinition bestTerm = null;
        int bestStart = Integer.MAX_VALUE;

        for (int i = 0; i < compiledPatterns.size(); i++) {
            Pattern pattern = compiledPatterns.get(i);
            if (pattern == null) {
                continue;
            }
            try {
                Matcher m = pattern.matcher(sanitizedPrompt);
                if (!m.find()) {
                    continue;
                }
                int start = m.start();
                if (start < bestStart) {
                    bestStart = start;
                    bestTerm = activeTerms.get(i);
                    if (bestStart == 0) {
                        break;
                    }
                }
            } catch (IllegalStateException ex) {
                LOG.log(Level.FINE, "Term pattern evaluation failed", ex);
            }
        }

        return bestTerm != null ? bestTerm.getName() : OTHER_PARASOFT_LABEL;
    }

    final String toChartJson(TermSummary summary) {
        if (summary == null || summary.getTermCounts().isEmpty()) {
            return "[]";
        }

        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Map.Entry<String, Integer> entry : summary.getTermCounts().entrySet()) {
            String label = entry.getKey() == null ? "" : entry.getKey();
            Integer boxedCount = entry.getValue();
            int count = boxedCount == null ? 0 : boxedCount.intValue();

            builder.add(Json.createObjectBuilder()
                    .add("label", label)
                    .add("count", count)
                    .add("term", label));
        }
        return builder.build().toString();
    }
}
