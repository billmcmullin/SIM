package com.sim.chatserver.render;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.util.SessionLabelStore;

public final class DashboardRowsRenderer {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DashboardRowsRenderer() {
    }

    public static String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return "<tr><td colspan=\"5\" class=\"empty-row\">No widget activity found.</td></tr>";
        }

        StringBuilder b = new StringBuilder(Math.max(256, stats.size() * 240));
        for (WidgetStat stat : stats) {
            String widgetId = stat.getWidgetId() == null ? "" : stat.getWidgetId();
            String label = stat.getLabel() == null ? widgetId : stat.getLabel();

            String baseWidgetHref = contextPath + "/dashboard/widgets/view?widgetId="
                    + URLEncoder.encode(widgetId, StandardCharsets.UTF_8);

            int today = stat.getTodayCount();
            int yesterday = stat.getYesterdayCount();
            int delta = stat.getDelta();

            String todayDate = LocalDate.now(ZoneId.systemDefault()).toString();
            String yesterdayDate = LocalDate.now(ZoneId.systemDefault()).minusDays(1).toString();

            String todayHref = contextPath + "/dashboard/widgets/view?widgetId="
                    + URLEncoder.encode(widgetId, StandardCharsets.UTF_8)
                    + "&date=" + URLEncoder.encode(todayDate, StandardCharsets.UTF_8);

            String yesterdayHref = contextPath + "/dashboard/widgets/view?widgetId="
                    + URLEncoder.encode(widgetId, StandardCharsets.UTF_8)
                    + "&date=" + URLEncoder.encode(yesterdayDate, StandardCharsets.UTF_8);

            String todayCell = today > 0
                    ? "<a class=\"metric-link\" href=\"" + todayHref + "\">" + today + "</a>"
                    : String.valueOf(today);

            String yesterdayCell = yesterday > 0
                    ? "<a class=\"metric-link\" href=\"" + yesterdayHref + "\">" + yesterday + "</a>"
                    : String.valueOf(yesterday);

            String deltaText = (delta > 0 ? "+" : "") + delta;
            String deltaClass = switch (stat.getDirection()) {
                case "up" ->
                    "progression-up";
                case "down" ->
                    "progression-down";
                default ->
                    "progression-flat";
            };

            b.append("<tr>")
                    .append("<td><a class=\"metric-link\" href=\"").append(baseWidgetHref).append("\">")
                    .append(escapeHtml(label)).append("</a></td>")
                    .append("<td>").append(stat.getCount()).append("</td>")
                    .append("<td>").append(todayCell).append("</td>")
                    .append("<td>").append(yesterdayCell).append("</td>")
                    .append("<td><span class=\"").append(deltaClass).append("\">")
                    .append(escapeHtml(deltaText))
                    .append("</span></td>")
                    .append("</tr>");
        }
        return b.toString();
    }

    public static String renderDailyTopTermsRows(List<TopTopic> terms, String contextPath) {
        if (terms == null || terms.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"empty-row\">No term activity for today/yesterday.</td></tr>";
        }

        StringBuilder b = new StringBuilder(Math.max(256, terms.size() * 220));
        int rank = 1;

        for (TopTopic t : terms) {
            String label = t.getLabel() == null ? "" : t.getLabel();
            String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);

            String termHref = contextPath + "/dashboard/term-review?term=" + encodedLabel;

            String todayHref = contextPath + "/dashboard/term-review?term="
                    + encodedLabel + "&mode=increaseOnly";

            // FIX: yesterday now explicitly uses yesterdayOnly mode
            String yesterdayHref = contextPath + "/dashboard/term-review?term="
                    + encodedLabel + "&mode=yesterdayOnly";

            String todayCell = t.getToday() > 0
                    ? "<a class=\"metric-link\" href=\"" + todayHref + "\">" + t.getToday() + "</a>"
                    : String.valueOf(t.getToday());

            String yesterdayCell = t.getYesterday() > 0
                    ? "<a class=\"metric-link\" href=\"" + yesterdayHref + "\">" + t.getYesterday() + "</a>"
                    : String.valueOf(t.getYesterday());

            b.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td><a class=\"metric-link\" href=\"").append(termHref).append("\">")
                    .append(escapeHtml(label)).append("</a></td>")
                    .append("<td>").append(todayCell).append("</td>")
                    .append("<td>").append(yesterdayCell).append("</td>")
                    .append("</tr>");
        }

        return b.toString();
    }

    public static String renderOtherParasoftLatestRows(List<OtherParasoftEntry> rows, String contextPath) {
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"5\" class=\"empty-row\">No recent matches.</td></tr>";
        }

        StringBuilder b = new StringBuilder(Math.max(256, rows.size() * 220));
        int rank = 1;

        for (OtherParasoftEntry row : rows) {
            String widget = nullSafe(row.getWidgetName());
            String prompt = nullSafe(row.getPrompt());
            String sessionId = nullSafe(row.getSessionId());
            String createdAt = formatTs(row.getCreatedAt());

            String sessionCell;
            if (!sessionId.isBlank()) {
                String href = contextPath + "/customer-profile?sessionId="
                        + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                sessionCell = "<a class=\"customer-profile-link\" href=\"" + href + "\">" + escapeHtml(sessionId) + "</a>";
            } else {
                sessionCell = "—";
            }

            b.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td>").append(escapeHtml(widget)).append("</td>")
                    .append("<td class=\"truncate\" title=\"").append(escapeHtml(prompt)).append("\">")
                    .append(escapeHtml(prompt)).append("</td>")
                    .append("<td>").append(sessionCell).append("</td>")
                    .append("<td>").append(escapeHtml(createdAt)).append("</td>")
                    .append("</tr>");
        }

        return b.toString();
    }

    public static String renderSessionRows(
            List<SessionStat> sessions,
            Map<String, SessionLabelStore.SessionLabel> labels,
            String contextPath
    ) {
        if (sessions == null || sessions.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"empty-row\">No session activity available.</td></tr>";
        }

        StringBuilder b = new StringBuilder(Math.max(256, sessions.size() * 220));
        int rank = 1;

        for (SessionStat s : sessions) {
            String sessionId = nullSafe(s.getSessionId());
            String last = nullSafe(s.getLastEntry());
            int count = s.getCount();

            SessionLabelStore.SessionLabel info = labels == null ? null : labels.get(sessionId);
            String displayName = extractDisplayName(info);
            String displayLabel = (displayName == null || displayName.isBlank()) ? sessionId : displayName;

            String reviewUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                    + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
            String profileUrl = contextPath + "/customer-profile?sessionId="
                    + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);

            b.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td><div><a class=\"customer-profile-link\" href=\"").append(profileUrl).append("\">")
                    .append(escapeHtml(displayLabel)).append("</a></div>");

            if (!displayLabel.equals(sessionId)) {
                b.append("<div class=\"session-id-muted\"><a class=\"customer-profile-link\" href=\"")
                        .append(profileUrl).append("\">")
                        .append(escapeHtml(sessionId))
                        .append("</a></div>");
            }

            b.append("</td>")
                    .append("<td><a class=\"metric-link\" href=\"").append(reviewUrl).append("\">")
                    .append(count).append(" chats</a></td>")
                    .append("<td>").append(escapeHtml(last)).append("</td>")
                    .append("</tr>");
        }

        return b.toString();
    }

    private static String extractDisplayName(SessionLabelStore.SessionLabel info) {
        return info == null ? null : info.getDisplayName();
    }

    private static String formatTs(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        try {
            return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(TS_FMT);
        } catch (Exception e) {
            return ts.toString();
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder((int) (value.length() * 1.2));
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
