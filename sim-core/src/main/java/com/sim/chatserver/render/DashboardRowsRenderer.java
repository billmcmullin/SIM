package com.sim.chatserver.render;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.util.SessionLabelStore;

public final class DashboardRowsRenderer {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOG = Logger.getLogger(DashboardRowsRenderer.class.getName());

    private DashboardRowsRenderer() {
    }

    public static String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return emptyRow(5, "No widget activity found.");
        }

        return renderHtml(Math.max(256, stats.size() * 240), w -> {
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

                String deltaText = (delta > 0 ? "+" : "") + delta;
                String deltaClass = switch (stat.getDirection()) {
                    case "up" -> "progression-up";
                    case "down" -> "progression-down";
                    default -> "progression-flat";
                };

                w.start("tr");

                w.start("td");
                w.anchor("metric-link", baseWidgetHref, label);
                w.end();

                w.element("td", String.valueOf(stat.getCount()));

                w.start("td");
                if (today > 0) {
                    w.anchor("metric-link", todayHref, String.valueOf(today));
                } else {
                    w.text(String.valueOf(today));
                }
                w.end();

                w.start("td");
                if (yesterday > 0) {
                    w.anchor("metric-link", yesterdayHref, String.valueOf(yesterday));
                } else {
                    w.text(String.valueOf(yesterday));
                }
                w.end();

                w.start("td");
                w.span(deltaClass, deltaText);
                w.end();

                w.end();
            }
        });
    }

    public static String renderDailyTopTermsRows(List<TopTopic> terms, String contextPath) {
        if (terms == null || terms.isEmpty()) {
            return emptyRow(4, "No term activity for today/yesterday.");
        }

        return renderHtml(Math.max(256, terms.size() * 220), w -> {
            int rank = 1;
            for (TopTopic t : terms) {
                String label = t.getLabel() == null ? "" : t.getLabel();
                String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);

                String termHref = contextPath + "/dashboard/term-review?term=" + encodedLabel;

                String todayHref = contextPath + "/dashboard/term-review?term="
                        + encodedLabel + "&mode=increaseOnly";

                String yesterdayHref = contextPath + "/dashboard/term-review?term="
                        + encodedLabel + "&mode=yesterdayOnly";

                w.start("tr");
                w.element("td", String.valueOf(rank++));

                w.start("td");
                w.anchor("metric-link", termHref, label);
                w.end();

                w.start("td");
                if (t.getToday() > 0) {
                    w.anchor("metric-link", todayHref, String.valueOf(t.getToday()));
                } else {
                    w.text(String.valueOf(t.getToday()));
                }
                w.end();

                w.start("td");
                if (t.getYesterday() > 0) {
                    w.anchor("metric-link", yesterdayHref, String.valueOf(t.getYesterday()));
                } else {
                    w.text(String.valueOf(t.getYesterday()));
                }
                w.end();

                w.end();
            }
        });
    }

    public static String renderOtherParasoftLatestRows(List<OtherParasoftEntry> rows, String contextPath) {
        if (rows == null || rows.isEmpty()) {
            return emptyRow(5, "No recent matches.");
        }

        return renderHtml(Math.max(256, rows.size() * 220), w -> {
            int rank = 1;
            for (OtherParasoftEntry row : rows) {
                String widget = nullSafe(row.getWidgetName());
                String prompt = nullSafe(row.getPrompt());
                String sessionId = nullSafe(row.getSessionId());
                String createdAt = formatTs(row.getCreatedAt());

                w.start("tr");
                w.element("td", String.valueOf(rank++));
                w.element("td", widget);

                w.start("td", "class", "truncate", "title", prompt);
                w.text(prompt);
                w.end();

                w.start("td");
                if (!sessionId.isBlank()) {
                    String href = contextPath + "/customer-profile?sessionId="
                            + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                    w.anchor("customer-profile-link", href, sessionId);
                } else {
                    w.text("—");
                }
                w.end();

                w.element("td", createdAt);
                w.end();
            }
        });
    }

    public static String renderSessionRows(
            List<SessionStat> sessions,
            Map<String, SessionLabelStore.SessionLabel> labels,
            String contextPath
    ) {
        if (sessions == null || sessions.isEmpty()) {
            return emptyRow(4, "No session activity available.");
        }

        return renderHtml(Math.max(256, sessions.size() * 220), w -> {
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

                w.start("tr");
                w.element("td", String.valueOf(rank++));

                w.start("td");
                w.start("div");
                w.anchor("customer-profile-link", profileUrl, displayLabel);
                w.end();

                if (!displayLabel.equals(sessionId)) {
                    w.start("div", "class", "session-id-muted");
                    w.anchor("customer-profile-link", profileUrl, sessionId);
                    w.end();
                }
                w.end();

                w.start("td");
                w.anchor("metric-link", reviewUrl, count + " chats");
                w.end();

                w.element("td", last);
                w.end();
            }
        });
    }

    private static String extractDisplayName(SessionLabelStore.SessionLabel info) {
        return info == null ? null : info.getDisplayName();
    }

    private static String formatTs(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        try {
            java.time.Instant instant = ts.toInstant();
            if (instant == null) {
                return "—";
            }
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(TS_FMT);
        } catch (DateTimeException | ArithmeticException e) {
            LOG.log(Level.FINE, "Unable to format timestamp", e);
            return ts.toString();
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String emptyRow(int colspan, String message) {
        return renderHtml(96, w -> {
            w.start("tr");
            w.start("td", "colspan", String.valueOf(colspan), "class", "empty-row");
            w.text(message);
            w.end();
            w.end();
        });
    }

    private static String renderHtml(int initialCapacity, HtmlRenderAction action) {
        StringWriter sink = new StringWriter(Math.max(64, initialCapacity));
        try {
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(sink);
            HtmlWriter w = new HtmlWriter(xml);
            action.render(w);
            xml.flush();
            xml.close();
            return sink.toString();
        } catch (XMLStreamException e) {
            LOG.log(Level.WARNING, "Unable to render dashboard rows", e);
            throw new IllegalStateException("Unable to render dashboard rows", e);
        }
    }

    @FunctionalInterface
    private interface HtmlRenderAction {

        void render(HtmlWriter writer) throws XMLStreamException;
    }

    private static final class HtmlWriter {

        final XMLStreamWriter xml;

        private HtmlWriter(XMLStreamWriter xml) {
            this.xml = xml;
        }

        private void start(String tag, String... attrs) throws XMLStreamException {
            xml.writeStartElement(tag);
            for (int i = 0; i + 1 < attrs.length; i += 2) {
                xml.writeAttribute(attrs[i], attrs[i + 1] == null ? "" : attrs[i + 1]);
            }
        }

        private void end() throws XMLStreamException {
            xml.writeEndElement();
        }

        private void text(String value) throws XMLStreamException {
            xml.writeCharacters(value == null ? "" : value);
        }

        private void element(String tag, String value) throws XMLStreamException {
            start(tag);
            text(value);
            end();
        }

        private void anchor(String cssClass, String href, String text) throws XMLStreamException {
            start("a", "class", cssClass, "href", href);
            text(text);
            end();
        }

        private void span(String cssClass, String text) throws XMLStreamException {
            start("span", "class", cssClass);
            text(text);
            end();
        }
    }
}
