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
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

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
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

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
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = ""; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

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
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = ""; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

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
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows7() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows8() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = ""; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows9() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows10() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows11() throws Throwable
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
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows12() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows13() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows14() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = ""; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = -1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult3 = "getLabelResult3"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult3);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows15() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = -1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows16() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = "getLabelResult2"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = -1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult3 = "getLabelResult3"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult3);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows17() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 1; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows18() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 1; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows19() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows20() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "\\"; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows21() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows22() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows23() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows24() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = ""; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult3 = "getLabelResult3"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult3);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderDailyTopTermsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderDailyTopTermsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderDailyTopTermsRows25() throws Throwable
    {
        // When
        List<TopTopic> terms = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayResult = 0; // UTA: configured value
        when(item.getToday()).thenReturn(getTodayResult);

        int getYesterdayResult = 0; // UTA: configured value
        when(item.getYesterday()).thenReturn(getYesterdayResult);
        terms.add(item);
        TopTopic item2 = mock(TopTopic.class);
        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayResult2 = 1; // UTA: default value
        when(item2.getToday()).thenReturn(getTodayResult2);

        int getYesterdayResult2 = 1; // UTA: default value
        when(item2.getYesterday()).thenReturn(getYesterdayResult2);
        terms.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);

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
        Timestamp getCreatedAtResult = null; // UTA: configured value
        when(item.getCreatedAt()).thenReturn(getCreatedAtResult);

        String getPromptResult = ""; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

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
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows7() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows8() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows9() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        String getWidgetNameResult = "getWidgetNameResult"; // UTA: default value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows10() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        String getPromptResult = "getPromptResult"; // UTA: default value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows11() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        String getPromptResult = null; // UTA: configured value
        when(item.getPrompt()).thenReturn(getPromptResult);

        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);

        String getWidgetNameResult = null; // UTA: configured value
        when(item.getWidgetName()).thenReturn(getWidgetNameResult);
        rows.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderOtherParasoftLatestRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderOtherParasoftLatestRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderOtherParasoftLatestRows12() throws Throwable
    {
        // When
        List<OtherParasoftEntry> rows = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        Timestamp getCreatedAtResult = mock(Timestamp.class);
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
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows3() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows4() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = ""; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows5() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows6() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = mock(Map.class);
        SessionLabel getResult = null; // UTA: configured value
        when(labels.get(nullable(Object.class))).thenReturn(getResult);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows7() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows8() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getSessionIdResult = "getSessionIdResult"; // UTA: default value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows9() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = "getLastEntryResult"; // UTA: default value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows10() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        SessionStat item2 = mock(SessionStat.class);
        String getLastEntryResult2 = "getLastEntryResult2"; // UTA: default value
        when(item2.getLastEntry()).thenReturn(getLastEntryResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        sessions.add(item2);
        Map<String, SessionLabel> labels = null; // UTA: configured value
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows11() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        SessionStat item2 = mock(SessionStat.class);
        String getLastEntryResult2 = "getLastEntryResult2"; // UTA: default value
        when(item2.getLastEntry()).thenReturn(getLastEntryResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        sessions.add(item2);
        Map<String, SessionLabel> labels = mock(Map.class);
        SessionLabel getResult = null; // UTA: configured value
        when(labels.get(nullable(Object.class))).thenReturn(getResult);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderSessionRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderSessionRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderSessionRows12() throws Throwable
    {
        // When
        List<SessionStat> sessions = new ArrayList<SessionStat>(); // UTA: default value
        SessionStat item = mock(SessionStat.class);
        String getLastEntryResult = null; // UTA: configured value
        when(item.getLastEntry()).thenReturn(getLastEntryResult);

        String getSessionIdResult = null; // UTA: configured value
        when(item.getSessionId()).thenReturn(getSessionIdResult);
        sessions.add(item);
        SessionStat item2 = mock(SessionStat.class);
        String getLastEntryResult2 = "getLastEntryResult2"; // UTA: default value
        when(item2.getLastEntry()).thenReturn(getLastEntryResult2);

        String getSessionIdResult2 = "getSessionIdResult2"; // UTA: default value
        when(item2.getSessionId()).thenReturn(getSessionIdResult2);
        sessions.add(item2);
        Map<String, SessionLabel> labels = new HashMap<String, SessionLabel>(); // UTA: default value
        String key = "key"; // UTA: default value
        SessionLabel value = mock(SessionLabel.class);
        labels.put(key, value);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);

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
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

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

        int getTodayCountResult = 1; // UTA: configured value
        when(item.getTodayCount()).thenReturn(getTodayCountResult);

        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows4() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows5() throws Throwable
    {
        // When
        List<WidgetStat> stats = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        int getDeltaResult = 1; // UTA: configured value
        when(item.getDelta()).thenReturn(getDeltaResult);

        String getDirectionResult = "getDirectionResult"; // UTA: default value
        when(item.getDirection()).thenReturn(getDirectionResult);

        String getLabelResult = "getLabelResult"; // UTA: default value
        String getLabelResult2 = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult, getLabelResult2);

        int getTodayCountResult = 0; // UTA: configured value
        when(item.getTodayCount()).thenReturn(getTodayCountResult);

        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows6() throws Throwable
    {
        // When
        List<WidgetStat> stats = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        int getDeltaResult = -1; // UTA: configured value
        when(item.getDelta()).thenReturn(getDeltaResult);

        String getDirectionResult = "getDirectionResult"; // UTA: default value
        when(item.getDirection()).thenReturn(getDirectionResult);

        String getLabelResult = null; // UTA: configured value
        when(item.getLabel()).thenReturn(getLabelResult);

        int getTodayCountResult = 0; // UTA: configured value
        when(item.getTodayCount()).thenReturn(getTodayCountResult);

        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows7() throws Throwable
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

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows8() throws Throwable
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

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = ""; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows9() throws Throwable
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

        int getYesterdayCountResult = -1; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        WidgetStat item2 = mock(WidgetStat.class);
        int getDeltaResult2 = 1; // UTA: default value
        when(item2.getDelta()).thenReturn(getDeltaResult2);

        String getDirectionResult2 = "getDirectionResult2"; // UTA: default value
        when(item2.getDirection()).thenReturn(getDirectionResult2);

        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayCountResult2 = 1; // UTA: default value
        when(item2.getTodayCount()).thenReturn(getTodayCountResult2);

        String getWidgetIdResult2 = "getWidgetIdResult2"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult2);

        int getYesterdayCountResult2 = 1; // UTA: default value
        when(item2.getYesterdayCount()).thenReturn(getYesterdayCountResult2);
        stats.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }

    /**
     * Parasoft Jtest UTA: Test for renderWidgetStatsRows(List, String)
     *
     * @see com.sim.chatserver.render.DashboardRowsRenderer#renderWidgetStatsRows(List, String)
     * @author bmcmullin
     */
    @Test
    public void testRenderWidgetStatsRows10() throws Throwable
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

        int getYesterdayCountResult = 0; // UTA: configured value
        when(item.getYesterdayCount()).thenReturn(getYesterdayCountResult);
        stats.add(item);
        WidgetStat item2 = mock(WidgetStat.class);
        int getDeltaResult2 = 1; // UTA: default value
        when(item2.getDelta()).thenReturn(getDeltaResult2);

        String getDirectionResult2 = "getDirectionResult2"; // UTA: default value
        when(item2.getDirection()).thenReturn(getDirectionResult2);

        String getLabelResult2 = "getLabelResult2"; // UTA: default value
        when(item2.getLabel()).thenReturn(getLabelResult2);

        int getTodayCountResult2 = 1; // UTA: default value
        when(item2.getTodayCount()).thenReturn(getTodayCountResult2);

        String getWidgetIdResult2 = "getWidgetIdResult2"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult2);

        int getYesterdayCountResult2 = 1; // UTA: default value
        when(item2.getYesterdayCount()).thenReturn(getYesterdayCountResult2);
        stats.add(item2);
        String contextPath = "contextPath"; // UTA: default value
        String result = DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);

    }
}
