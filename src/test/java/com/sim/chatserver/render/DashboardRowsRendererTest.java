package com.sim.chatserver.render;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.util.SessionLabelStore.SessionLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardRowsRenderer
 *
 * @see com.sim.chatserver.render.DashboardRowsRenderer
 * @author bmcmullin
 */
public class DashboardRowsRendererTest
{

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows() throws Throwable
    {
        // When
        List<TopTopic> terms = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td colspan=\"4\" class=\"empty-row\">No term activity for today/yesterday.</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows2() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td>1</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/term-review?term=\"></a></td><td><a class=\"metric-link\" href=\"contextPath/dashboard/topics?day=2026-04-27&q=\">1</a></td><td><a class=\"metric-link\" href=\"contextPath/dashboard/topics?day=2026-04-26&q=\">1</a></td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows3() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = "getLabelResult2"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td>1</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/term-review?term=getLabelResult2\">getLabelResult2</a></td><td>0</td><td>0</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows4() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "&*"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = -2; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td>1</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/term-review?term=%26*\">&amp;*</a></td><td>-2</td><td>0</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows5() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "<*"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = -2; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td>1</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/term-review?term=%3C*\">&lt;*</a></td><td>-2</td><td>0</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows6() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = ">*"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = -2; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

        // Then - assertions for result of method renderDailyTopTermsRows(List, String)
        assertEquals("<tr><td>1</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/term-review?term=%3E*\">&gt;*</a></td><td>-2</td><td>0</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td colspan=\"5\" class=\"empty-row\">No recent matches.</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows2() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td colspan=\"5\" class=\"empty-row\">No recent matches.</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows3() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = ""; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td>1</td><td></td><td class=\"truncate\" title=\"\"></td><td>—</td><td>—</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows4() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td>1</td><td></td><td class=\"truncate\" title=\"\"></td><td><a class=\"customer-profile-link\" href=\"contextPath/customer-profile?sessionId=getSessionIdResult\">getSessionIdResult</a></td><td>—</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows5() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult = mock(Timestamp.class);
        Instant toInstantResult = mock(Instant.class);
        ZonedDateTime atZoneResult = mock(ZonedDateTime.class);
        LocalDateTime toLocalDateTimeResult = mock(LocalDateTime.class);
        String formatResult = null; // UTA: configured value
        when(toLocalDateTimeResult.format(nullable(DateTimeFormatter.class))).thenReturn(formatResult);
        when(atZoneResult.toLocalDateTime()).thenReturn(toLocalDateTimeResult);
        when(toInstantResult.atZone(nullable(ZoneId.class))).thenReturn(atZoneResult);
        when(getCreatedAtResult.toInstant()).thenReturn(toInstantResult);
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td>1</td><td></td><td class=\"truncate\" title=\"\"></td><td>—</td><td></td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows6() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        OtherParasoftEntry item2 = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult2 = mock(Timestamp.class);
        when(item2.getCreatedAt()).thenReturn(getCreatedAtResult2);

        String getPromptResult2 = "getPromptResult2"; // UTA: default value
        when(item2.getPrompt()).thenReturn(getPromptResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);

        String getWidgetNameResult2 = "getWidgetNameResult2"; // UTA: default value
        when(item2.getWidgetName()).thenReturn(getWidgetNameResult2);
        rows.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

        // Then - assertions for result of method renderOtherParasoftLatestRows(List, String)
        assertEquals("<tr><td>1</td><td></td><td class=\"truncate\" title=\"\"></td><td>—</td><td>—</td></tr><tr><td>2</td><td>getWidgetNameResult2</td><td class=\"truncate\" title=\"getPromptResult2\">getPromptResult2</td><td><a class=\"customer-profile-link\" href=\"contextPath/customer-profile?sessionId=getSessionIdResult2\">getSessionIdResult2</a></td><td>Mock for Timestamp, hashCode: 1608757336</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows() throws Throwable
    {
        // When
        List<SessionStat> sessions = null; // UTA: configured value
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

        // Then - assertions for result of method renderSessionRows(List, Map, String)
        assertEquals("<tr><td colspan=\"4\" class=\"empty-row\">No session activity available.</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows2() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = ""; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

        // Then - assertions for result of method renderSessionRows(List, Map, String)
        assertEquals("<tr><td>1</td><td><div><a class=\"customer-profile-link\" href=\"contextPath/customer-profile?sessionId=\"></a></div></td><td><a class=\"metric-link\" href=\"contextPath/dashboard/sessions/drilldown/session-review?sessionId=\">0 chats</a></td><td></td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows() throws Throwable
    {
        // When
        List<WidgetStat> stats = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

        // Then - assertions for result of method renderWidgetStatsRows(List, String)
        assertEquals("<tr><td colspan=\"5\" class=\"empty-row\">No widget activity found.</td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows2() throws Throwable
    {
        // When
        List<WidgetStat> stats = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        int getDeltaResult = 1; // UTA: configured value
        when(item.getDelta()).thenReturn(getDeltaResult);

        String getDirectionResult = "getDirectionResult"; // UTA: default value
        when(item.getDirection()).thenReturn(getDirectionResult);

        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayCountResult = 1; // UTA: configured value
        when(item.getTodayCount()).thenReturn(getTodayCountResult);

        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

        // Then - assertions for result of method renderWidgetStatsRows(List, String)
        assertEquals("<tr><td><a class=\"metric-link\" href=\"contextPath/dashboard/widgets/view?widgetId=\"></a></td><td>0</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/widgets/view?widgetId=&date=2026-04-27\">1</a></td><td>0</td><td><span class=\"progression-flat\">+1</span></td></tr>", result);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows3() throws Throwable
    {
        // When
        List<WidgetStat> stats = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        int getDeltaResult = 1; // UTA: configured value
        when(item.getDelta()).thenReturn(getDeltaResult);

        String getDirectionResult = "getDirectionResult"; // UTA: default value
        when(item.getDirection()).thenReturn(getDirectionResult);

        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayCountResult = 0; // UTA: configured value
        when(item.getTodayCount()).thenReturn(getTodayCountResult);

        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);

        int getYesterdayCountResult = 1; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

        // Then - assertions for result of method renderWidgetStatsRows(List, String)
        assertEquals("<tr><td><a class=\"metric-link\" href=\"contextPath/dashboard/widgets/view?widgetId=\"></a></td><td>0</td><td>0</td><td><a class=\"metric-link\" href=\"contextPath/dashboard/widgets/view?widgetId=&date=2026-04-26\">1</a></td><td><span class=\"progression-flat\">+1</span></td></tr>", result);

    }

}
