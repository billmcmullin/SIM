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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.model.TermSummary;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlLiteralConstants;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Term-focused dashboard logic (term summary + chart payload).
 */
@Dependent
public class DashboardTermService {

    private static final Logger LOG = Logger.getLogger(DashboardTermService.class.getCanonicalName());

    private static final String UNUSED_SUPPRESSION = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String MSG_LOAD_ALL_TERMS_FALLBACK = "loadAllTerms fallback to empty list";
    private static final String SQL_SELECT_CHAT_ROWS = SqlLiteralConstants.SQL_SELECT_WIDGET_CHAT_PROMPT_RESPONSE_CREATED_SESSION_FROM;
    private static final String SQL_WHERE_CREATED_AT_RANGE = SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    private static final String COL_WIDGET_CHAT_ID = SqlLiteralConstants.COL_WIDGET_CHAT_ID;
    private static final String COL_PROMPT = SqlLiteralConstants.COL_PROMPT;
    private static final String COL_RESPONSE_TEXT = SqlLiteralConstants.COL_RESPONSE_TEXT;
    private static final String COL_CREATED_AT = SqlLiteralConstants.COL_CREATED_AT;
    private static final String COL_SESSION_ID = SqlLiteralConstants.COL_SESSION_ID;
    private static final String EMPTY = "";
    private static final String MSG_TERM_PATTERN_EVALUATION_FAILED = "Term pattern evaluation failed";
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final String JSON_ARRAY_START = "[";
    private static final String DEFAULT_COUNT_TEXT = "0";
    private static final String MSG_TERM_COUNT_PARSE_FALLBACK = "Unable to parse term count; using zero fallback";
    private static final String JSON_LABEL_PREFIX = "{\"label\":\"";
    private static final String JSON_COUNT_PART = "\",\"count\":";
    private static final String JSON_TERM_PART = ",\"term\":\"";
    private static final String JSON_OBJECT_SUFFIX = "\"}";
    private static final String ESCAPE_BACKSLASH_FROM = "\\";
    private static final String ESCAPE_BACKSLASH_TO = CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO;
    private static final String ESCAPE_QUOTE_FROM = "\"";
    private static final String ESCAPE_QUOTE_TO = CommonLiteralConstants.JSON_ESCAPE_QUOTE_TO;
    private static final String ESCAPE_NEWLINE_TO = CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO;

    public static final String OTHER_PARASOFT_LABEL = TermsStore.OTHER_PARASOFT_LABEL;

    private final TermsStore termsStore;

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(UNUSED_SUPPRESSION)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
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
            LOG.log(Level.FINE, MSG_LOAD_ALL_TERMS_FALLBACK, e);
            return List.of();
        }
    }

    /**
     * Existing behavior: all-time (no date filter).
     */
    private TermSummary buildTermSummary(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        return buildTermSummary(conn, widgets, terms, null, null);
    }

    private final TermSummary buildTermSummaryForDashboard(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        return buildTermSummary(conn, widgets, terms);
    }

    public final TermSummary computeTermSummaryForDashboard(Connection conn, List<WidgetEntry> widgets, List<TermDefinition> terms) throws SQLException {
        return buildTermSummaryForDashboard(conn, widgets, terms);
    }

    public final TermSummary computeTermSummaryForDashboard(
            Connection conn,
            List<WidgetEntry> widgets,
            List<TermDefinition> terms,
            LocalDate rangeStartInclusive,
            LocalDate rangeEndInclusive
    ) throws SQLException {
        return buildTermSummary(conn, widgets, terms, rangeStartInclusive, rangeEndInclusive);
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
            sql = SQL_SELECT_CHAT_ROWS
                    + DashboardDbUtil.quoteIdentifier(tableName)
                + SQL_WHERE_CREATED_AT_RANGE;
        } else {
            sql = SQL_SELECT_CHAT_ROWS
                    + DashboardDbUtil.quoteIdentifier(tableName);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (useDateRange) {
                ps.setTimestamp(1, startTs);
                ps.setTimestamp(2, endExclusiveTs);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chatId = rs.getString(COL_WIDGET_CHAT_ID);
                    if (chatId == null) {
                        chatId = EMPTY;
                    }

                    String prompt = rs.getString(COL_PROMPT);
                    if (prompt == null) {
                        prompt = EMPTY;
                    }

                    String response = rs.getString(COL_RESPONSE_TEXT);
                    Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                    String sessionId = rs.getString(COL_SESSION_ID);

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
                LOG.log(Level.FINE, MSG_TERM_PATTERN_EVALUATION_FAILED, ex);
            }
        }

        return bestTerm != null ? bestTerm.getName() : OTHER_PARASOFT_LABEL;
    }

    final String toChartJson(TermSummary summary) {
        if (summary == null || summary.getTermCounts().isEmpty()) {
            return EMPTY_JSON_ARRAY;
        }

        StringBuilder out = new StringBuilder(JSON_ARRAY_START);
        boolean first = true;
        for (Map.Entry<String, Integer> entry : summary.getTermCounts().entrySet()) {
            String label = entry.getKey() == null ? EMPTY : entry.getKey();
            String countText = Objects.toString(entry.getValue(), DEFAULT_COUNT_TEXT);
            int count;
            try {
                count = Integer.parseInt(countText);
            } catch (NumberFormatException ex) {
                LOG.log(Level.FINE, MSG_TERM_COUNT_PARSE_FALLBACK, ex);
                count = 0;
            }
            String escaped = escapeJson(label);

            if (!first) {
                out.append(',');
            }
            first = false;

            out.append(JSON_LABEL_PREFIX)
                    .append(escaped)
                    .append(JSON_COUNT_PART)
                    .append(count)
                    .append(JSON_TERM_PART)
                    .append(escaped)
                    .append(JSON_OBJECT_SUFFIX);
        }
        out.append(']');
        return out.toString();
    }

    private String escapeJson(String value) {
        return value.replace(ESCAPE_BACKSLASH_FROM, ESCAPE_BACKSLASH_TO)
                .replace(ESCAPE_QUOTE_FROM, ESCAPE_QUOTE_TO)
                .replace(System.lineSeparator(), ESCAPE_NEWLINE_TO)
                .replace(System.lineSeparator(), EMPTY);
    }
}

