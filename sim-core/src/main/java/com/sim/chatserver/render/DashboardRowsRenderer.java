package com.sim.chatserver.render;

import com.sim.chatserver.util.SessionLabel;
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

import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.SessionStat;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.SessionLabelStore;

public final class DashboardRowsRenderer {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern(CommonLiteralConstants.DATETIME_PATTERN_SECONDS);
    private static final Logger LOG = Logger.getLogger(DashboardRowsRenderer.class.getCanonicalName());
    static final String EMPTY = "";
    private static final String PLUS = "+";
    private static final String DIR_UP = "up";
    private static final String DIR_DOWN = com.sim.chatserver.util.CommonLiteralConstants.VALUE_DOWN_LOWER;
    private static final String CLASS_PROGRESSION_UP = "progression-up";
    private static final String CLASS_PROGRESSION_DOWN = "progression-down";
    private static final String CLASS_PROGRESSION_FLAT = "progression-flat";
    private static final String TAG_TR = "tr";
    private static final String TAG_TD = "td";
    private static final String TAG_DIV = "div";
    static final String TAG_A = "a";
    static final String TAG_SPAN = "span";
    static final String ATTR_CLASS = "class";
    static final String ATTR_HREF = "href";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_COLSPAN = "colspan";
    private static final String CLASS_METRIC_LINK = "metric-link";
    private static final String CLASS_CUSTOMER_PROFILE_LINK = "customer-profile-link";
    private static final String CLASS_TRUNCATE = "truncate";
    private static final String CLASS_SESSION_ID_MUTED = "session-id-muted";
    private static final String CLASS_EMPTY_ROW = "empty-row";
    private static final String HREF_WIDGET_VIEW = "/dashboard/widgets/view?widgetId=";
    private static final String HREF_TERM_REVIEW = "/dashboard/term-review?term=";
    private static final String HREF_SESSION_REVIEW = "/dashboard/sessions/drilldown/session-review?sessionId=";
    private static final String HREF_CUSTOMER_PROFILE = "/customer-profile?sessionId=";
    private static final String QUERY_DATE = "&date=";
    private static final String QUERY_MODE_INCREASE = "&mode=increaseOnly";
    private static final String QUERY_MODE_YESTERDAY = "&mode=yesterdayOnly";
    private static final String CHATS_SUFFIX = " chats";
    private static final String PLACEHOLDER_DASH = "Ã¢â‚¬â€";
    private static final String MSG_NO_WIDGET_ACTIVITY = "No widget activity found.";
    private static final String MSG_NO_TERM_ACTIVITY = "No term activity for today/yesterday.";
    private static final String MSG_NO_RECENT_MATCHES = "No recent matches.";
    private static final String MSG_NO_SESSION_ACTIVITY = "No session activity available.";
    private static final String MSG_UNABLE_FORMAT_TIMESTAMP = "Unable to format timestamp";
    private static final String MSG_UNABLE_RENDER_DASHBOARD_ROWS = "Unable to render dashboard rows";

    private DashboardRowsRenderer() {
    }

    public static String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        if (stats == null || stats.isEmpty()) {
            return emptyRow(5, MSG_NO_WIDGET_ACTIVITY);
        }

        return renderHtml(Math.max(256, stats.size() * 240), w -> {
            for (WidgetStat stat : stats) {
                String widgetId = stat.getWidgetId() == null ? EMPTY : stat.getWidgetId();
                String label = stat.getLabel() == null ? widgetId : stat.getLabel();

                String baseWidgetHref = contextPath + HREF_WIDGET_VIEW
                        + URLEncoder.encode(widgetId, StandardCharsets.UTF_8);

                int today = stat.getTodayCount();
                int yesterday = stat.getYesterdayCount();
                int delta = stat.getDelta();

                String todayDate = LocalDate.now(ZoneId.systemDefault()).toString();
                String yesterdayDate = LocalDate.now(ZoneId.systemDefault()).minusDays(1).toString();

                String todayHref = contextPath + HREF_WIDGET_VIEW
                        + URLEncoder.encode(widgetId, StandardCharsets.UTF_8)
                        + QUERY_DATE + URLEncoder.encode(todayDate, StandardCharsets.UTF_8);

                String yesterdayHref = contextPath + HREF_WIDGET_VIEW
                        + URLEncoder.encode(widgetId, StandardCharsets.UTF_8)
                        + QUERY_DATE + URLEncoder.encode(yesterdayDate, StandardCharsets.UTF_8);

                String deltaText = (delta > 0 ? PLUS : EMPTY) + delta;
                String deltaClass = switch (stat.getDirection()) {
                    case DIR_UP -> CLASS_PROGRESSION_UP;
                    case DIR_DOWN -> CLASS_PROGRESSION_DOWN;
                    default -> CLASS_PROGRESSION_FLAT;
                };

                w.start(TAG_TR);

                w.start(TAG_TD);
                w.anchor(CLASS_METRIC_LINK, baseWidgetHref, label);
                w.end();

                w.element(TAG_TD, String.valueOf(stat.getCount()));

                w.start(TAG_TD);
                if (today > 0) {
                    w.anchor(CLASS_METRIC_LINK, todayHref, String.valueOf(today));
                } else {
                    w.text(String.valueOf(today));
                }
                w.end();

                w.start(TAG_TD);
                if (yesterday > 0) {
                    w.anchor(CLASS_METRIC_LINK, yesterdayHref, String.valueOf(yesterday));
                } else {
                    w.text(String.valueOf(yesterday));
                }
                w.end();

                w.start(TAG_TD);
                w.span(deltaClass, deltaText);
                w.end();

                w.end();
            }
        });
    }

    public static String renderDailyTopTermsRows(List<TopTopic> terms, String contextPath) {
        if (terms == null || terms.isEmpty()) {
            return emptyRow(4, MSG_NO_TERM_ACTIVITY);
        }

        return renderHtml(Math.max(256, terms.size() * 220), w -> {
            int rank = 1;
            for (TopTopic t : terms) {
                String label = t.getLabel() == null ? EMPTY : t.getLabel();
                String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);

                String termHref = contextPath + HREF_TERM_REVIEW + encodedLabel;

                String todayHref = contextPath + HREF_TERM_REVIEW
                        + encodedLabel + QUERY_MODE_INCREASE;

                String yesterdayHref = contextPath + HREF_TERM_REVIEW
                        + encodedLabel + QUERY_MODE_YESTERDAY;

                w.start(TAG_TR);
                w.element(TAG_TD, String.valueOf(rank++));

                w.start(TAG_TD);
                w.anchor(CLASS_METRIC_LINK, termHref, label);
                w.end();

                w.start(TAG_TD);
                if (t.getToday() > 0) {
                    w.anchor(CLASS_METRIC_LINK, todayHref, String.valueOf(t.getToday()));
                } else {
                    w.text(String.valueOf(t.getToday()));
                }
                w.end();

                w.start(TAG_TD);
                if (t.getYesterday() > 0) {
                    w.anchor(CLASS_METRIC_LINK, yesterdayHref, String.valueOf(t.getYesterday()));
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
            return emptyRow(5, MSG_NO_RECENT_MATCHES);
        }

        return renderHtml(Math.max(256, rows.size() * 220), w -> {
            int rank = 1;
            for (OtherParasoftEntry row : rows) {
                String widget = nullSafe(row.getWidgetName());
                String prompt = nullSafe(row.getPrompt());
                String sessionId = nullSafe(row.getSessionId());
                String createdAt = formatTs(row.getCreatedAt());

                w.start(TAG_TR);
                w.element(TAG_TD, String.valueOf(rank++));
                w.element(TAG_TD, widget);

                w.start(TAG_TD, ATTR_CLASS, CLASS_TRUNCATE, ATTR_TITLE, prompt);
                w.text(prompt);
                w.end();

                w.start(TAG_TD);
                if (!sessionId.isBlank()) {
                    String href = contextPath + HREF_CUSTOMER_PROFILE
                            + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                    w.anchor(CLASS_CUSTOMER_PROFILE_LINK, href, sessionId);
                } else {
                    w.text(PLACEHOLDER_DASH);
                }
                w.end();

                w.element(TAG_TD, createdAt);
                w.end();
            }
        });
    }

        public static String renderSessionRows(
            List<SessionStat> sessions,
            Map<String, SessionLabel> labels,
            String contextPath
    ) {
        if (sessions == null || sessions.isEmpty()) {
            return emptyRow(4, MSG_NO_SESSION_ACTIVITY);
        }

        return renderHtml(Math.max(256, sessions.size() * 220), w -> {
            int rank = 1;
            for (SessionStat s : sessions) {
                String sessionId = nullSafe(s.getSessionId());
                String last = nullSafe(s.getLastEntry());
                int count = s.getCount();

                SessionLabel info = labels == null ? null : labels.get(sessionId);
                String displayName = extractDisplayName(info);
                String displayLabel = (displayName == null || displayName.isBlank()) ? sessionId : displayName;

                String reviewUrl = contextPath + HREF_SESSION_REVIEW
                        + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                String profileUrl = contextPath + HREF_CUSTOMER_PROFILE
                        + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);

                w.start(TAG_TR);
                w.element(TAG_TD, String.valueOf(rank++));

                w.start(TAG_TD);
                w.start(TAG_DIV);
                w.anchor(CLASS_CUSTOMER_PROFILE_LINK, profileUrl, displayLabel);
                w.end();

                if (!displayLabel.equals(sessionId)) {
                    w.start(TAG_DIV, ATTR_CLASS, CLASS_SESSION_ID_MUTED);
                    w.anchor(CLASS_CUSTOMER_PROFILE_LINK, profileUrl, sessionId);
                    w.end();
                }
                w.end();

                w.start(TAG_TD);
                w.anchor(CLASS_METRIC_LINK, reviewUrl, count + CHATS_SUFFIX);
                w.end();

                w.element(TAG_TD, last);
                w.end();
            }
        });
    }

    private static String extractDisplayName(SessionLabel info) {
        return info == null ? null : info.getDisplayName();
    }

    private static String formatTs(Timestamp ts) {
        if (ts == null) {
            return PLACEHOLDER_DASH;
        }
        try {
            java.time.Instant instant = ts.toInstant();
            if (instant == null) {
                return PLACEHOLDER_DASH;
            }
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(TS_FMT);
        } catch (DateTimeException | ArithmeticException e) {
            LOG.log(Level.FINE, MSG_UNABLE_FORMAT_TIMESTAMP, e);
            return ts.toString();
        }
    }

    private static String nullSafe(String s) {
        return s == null ? EMPTY : s;
    }

    private static String emptyRow(int colspan, String message) {
        return renderHtml(96, w -> {
            w.start(TAG_TR);
            w.start(TAG_TD, ATTR_COLSPAN, String.valueOf(colspan), ATTR_CLASS, CLASS_EMPTY_ROW);
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
            LOG.log(Level.WARNING, MSG_UNABLE_RENDER_DASHBOARD_ROWS, e);
            throw new IllegalStateException(MSG_UNABLE_RENDER_DASHBOARD_ROWS, e);
        }
    }

    @FunctionalInterface
    private interface HtmlRenderAction {

        void render(HtmlWriter writer) throws XMLStreamException;
    }



}

final class HtmlWriter {

    final XMLStreamWriter xml;

    HtmlWriter(XMLStreamWriter xml) {
        this.xml = xml;
    }

    void start(String tag, String... attrs) throws XMLStreamException {
        xml.writeStartElement(tag);
        for (int i = 0; i + 1 < attrs.length; i += 2) {
            xml.writeAttribute(attrs[i], attrs[i + 1] == null ? DashboardRowsRenderer.EMPTY : attrs[i + 1]);
        }
    }

    void end() throws XMLStreamException {
        xml.writeEndElement();
    }

    void text(String value) throws XMLStreamException {
        xml.writeCharacters(value == null ? DashboardRowsRenderer.EMPTY : value);
    }

    void element(String tag, String value) throws XMLStreamException {
        start(tag);
        text(value);
        end();
    }

    void anchor(String cssClass, String href, String text) throws XMLStreamException {
        start(DashboardRowsRenderer.TAG_A, DashboardRowsRenderer.ATTR_CLASS, cssClass, DashboardRowsRenderer.ATTR_HREF, href);
        text(text);
        end();
    }

    void span(String cssClass, String text) throws XMLStreamException {
        start(DashboardRowsRenderer.TAG_SPAN, DashboardRowsRenderer.ATTR_CLASS, cssClass);
        text(text);
        end();
    }
}

