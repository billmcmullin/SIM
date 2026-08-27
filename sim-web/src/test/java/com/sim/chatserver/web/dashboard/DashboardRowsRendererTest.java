package com.sim.chatserver.web.dashboard;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.util.SessionLabelStore;

class DashboardRowsRendererTest {

    @Test
    void renderWidgetStatsRows_emptyList_rendersEmptyRow() {
        String html = DashboardRowsRenderer.renderWidgetStatsRows(List.of(), "/chat-server");

        assertTrue(html.contains("empty-row"));
        assertTrue(html.contains("No widget activity found."));
        assertTrue(html.contains("colspan=\"5\""));
    }

    @Test
    void renderWidgetStatsRows_populated_rendersLinksAndDeltaClasses() {
        List<WidgetStat> stats = List.of(
                new WidgetStat("w 1", "Label <One>", 42, 5, 3),
                new WidgetStat("w2", "Label Two", 8, 0, 2),
                new WidgetStat("w3", "Label Three", 0, 0, 0)
        );

        String html = DashboardRowsRenderer.renderWidgetStatsRows(stats, "/chat-server");
        String today = LocalDate.now(ZoneId.systemDefault()).toString();
        String yesterday = LocalDate.now(ZoneId.systemDefault()).minusDays(1).toString();

        assertTrue(html.contains("/chat-server/dashboard/widgets/view?widgetId=w+1"));
        assertTrue(html.contains("/chat-server/dashboard/widgets/view?widgetId=w+1&amp;date=" + today));
        assertTrue(html.contains("/chat-server/dashboard/widgets/view?widgetId=w+1&amp;date=" + yesterday));

        assertTrue(html.contains("Label &lt;One&gt;"));
        assertTrue(html.contains("class=\"progression-up\""));
        assertTrue(html.contains("class=\"progression-down\""));
        assertTrue(html.contains("class=\"progression-flat\""));
        assertTrue(html.contains(">+2<"));
        assertTrue(html.contains(">-2<"));
        assertTrue(html.contains(">0<"));
    }

    @Test
    void renderDailyTopTermsRows_populated_rendersEncodedTermLinks() {
        List<TopTopic> terms = List.of(
                new TopTopic("term A&B", 4, 1),
                new TopTopic("term B", 0, 0)
        );

        String html = DashboardRowsRenderer.renderDailyTopTermsRows(terms, "/chat-server");

        assertTrue(html.contains("/chat-server/dashboard/term-review?term=term+A%26B"));
        assertTrue(html.contains("mode=increaseOnly"));
        assertTrue(html.contains("mode=yesterdayOnly"));
        assertTrue(html.contains("term A&amp;B"));
        assertTrue(html.contains(">4<"));
    }

    @Test
    void renderOtherParasoftLatestRows_populated_rendersPromptSessionAndTimestamp() {
        List<OtherParasoftEntry> rows = List.of(
                new OtherParasoftEntry(
                        "wid-1",
                        "Widget One",
                        "Prompt <b>& details",
                        "session 1",
                        Timestamp.from(Instant.parse("2026-08-27T12:34:56Z"))
                ),
                new OtherParasoftEntry("wid-2", "Widget Two", "Simple", "", null)
        );

        String html = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, "/chat-server");

        assertTrue(html.contains("Widget One"));
        assertTrue(html.contains("class=\"truncate\""));
        assertTrue(html.contains("title=\"Prompt &lt;b&gt;&amp; details\""));
        assertTrue(html.contains("/chat-server/customer-profile?sessionId=session+1"));
        assertTrue(html.contains("2026-08-27 05:34:56") || html.contains("2026-08-27 12:34:56"));
        assertTrue(html.contains(">\u2014<"));
    }

    @Test
    void renderSessionRows_populated_rendersFriendlyAndSessionIdRows() throws Exception {
        List<SessionStat> sessions = List.of(
                new SessionStat("session/one", 3, "2026-08-27 10:00:00"),
                new SessionStat("session-two", 1, "2026-08-27 11:00:00")
        );
        Map<String, SessionLabelStore.SessionLabel> labels = Map.of(
                "session/one", newSessionLabel("Friendly User", "friendly@example.com")
        );

        String html = DashboardRowsRenderer.renderSessionRows(sessions, labels, "/chat-server");

        assertTrue(html.contains("Friendly User"));
        assertTrue(html.contains("session-id-muted"));
        assertTrue(html.contains("/chat-server/customer-profile?sessionId=session%2Fone"));
        assertTrue(html.contains("/chat-server/dashboard/sessions/drilldown/session-review?sessionId=session%2Fone"));
        assertTrue(html.contains(">3 chats<"));
        assertTrue(html.contains("session-two"));
    }

    @Test
    void renderSessionRows_emptyList_rendersEmptyRow() {
        String html = DashboardRowsRenderer.renderSessionRows(List.of(), Map.of(), "/chat-server");

        assertTrue(html.contains("empty-row"));
        assertTrue(html.contains("No session activity available."));
        assertTrue(html.contains("colspan=\"4\""));
    }

    private SessionLabelStore.SessionLabel newSessionLabel(String displayName, String email) throws Exception {
        Constructor<SessionLabelStore.SessionLabel> ctor = SessionLabelStore.SessionLabel.class
                .getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(displayName, email);
    }
}
