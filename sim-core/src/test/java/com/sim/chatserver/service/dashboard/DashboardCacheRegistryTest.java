package com.sim.chatserver.service.dashboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardCacheRegistry
 *
 * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry
 * @author bmcmullin
 */
public class DashboardCacheRegistryTest
{

    /**
     * Parasoft Jtest UTA: Test for clearAll()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#clearAll()
     * @author bmcmullin
     */
    @Test
    public void testClearAll() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        underTest.clearAll();

    }

    /**
     * Parasoft Jtest UTA: Test for getChatProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getChatProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetChatProgression() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat getResult = mock(ProgressStat.class);
        when(loader.get()).thenReturn(getResult);
        ProgressStat result = underTest.getChatProgression(loader);

        // Then - assertions for result of method getChatProgression(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getChatProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getChatProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetChatProgression2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String chatProgressionDayKeyValue = "chatProgressionDayKeyValue"; // UTA: configured value
        setPrivateField(underTest, DashboardCacheRegistry.class, "chatProgressionDayKey", chatProgressionDayKeyValue);

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat getResult = mock(ProgressStat.class);
        when(loader.get()).thenReturn(getResult);
        ProgressStat result = underTest.getChatProgression(loader);

        // Then - assertions for result of method getChatProgression(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Helper method to set private field chatProgressionDayKey
     */
    private static <T> void setPrivateField(Object object, Class<?> fieldClass, String fieldName, T value)
    {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException e) {
            throw (AssertionError) new AssertionError("No such field found").initCause(e);
        } catch (IllegalAccessException e) {
            throw (AssertionError) new AssertionError("Unable to access the specified private field").initCause(e);
        } catch (SecurityException e) {
            throw (AssertionError) new AssertionError("There was a security exception when attempting to access a private field").initCause(e);
        }
    }

    /**
     * Parasoft Jtest UTA: Test for getChatProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getChatProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetChatProgression3() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String chatProgressionDayKeyValue = "chatProgressionDayKeyValue"; // UTA: default value
        setPrivateField(underTest, DashboardCacheRegistry.class, "chatProgressionDayKey", chatProgressionDayKeyValue);

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat result = underTest.getChatProgression(loader);

        // Then - assertions for result of method getChatProgression(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getChatProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getChatProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetChatProgression4() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat result = underTest.getChatProgression(loader);

        // Then - assertions for result of method getChatProgression(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getDashboardProgressMetrics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getDashboardProgressMetrics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetDashboardProgressMetrics() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<DashboardProgressMetrics> loader = mock(Supplier.class);
        DashboardProgressMetrics getResult = mock(DashboardProgressMetrics.class);
        when(loader.get()).thenReturn(getResult);
        DashboardProgressMetrics result = underTest.getDashboardProgressMetrics(loader);

        // Then - assertions for result of method getDashboardProgressMetrics(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getDashboardProgressMetrics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getDashboardProgressMetrics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetDashboardProgressMetrics2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String dashboardProgressDayKeyValue = "dashboardProgressDayKeyValue"; // UTA: configured value
        setPrivateField(underTest, DashboardCacheRegistry.class, "dashboardProgressDayKey", dashboardProgressDayKeyValue);

        // When
        Supplier<DashboardProgressMetrics> loader = mock(Supplier.class);
        DashboardProgressMetrics getResult = mock(DashboardProgressMetrics.class);
        when(loader.get()).thenReturn(getResult);
        DashboardProgressMetrics result = underTest.getDashboardProgressMetrics(loader);

        // Then - assertions for result of method getDashboardProgressMetrics(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getDashboardProgressMetrics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getDashboardProgressMetrics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetDashboardProgressMetrics3() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String dashboardProgressDayKeyValue = "dashboardProgressDayKeyValue"; // UTA: default value
        setPrivateField(underTest, DashboardCacheRegistry.class, "dashboardProgressDayKey", dashboardProgressDayKeyValue);

        // When
        Supplier<DashboardProgressMetrics> loader = mock(Supplier.class);
        DashboardProgressMetrics result = underTest.getDashboardProgressMetrics(loader);

        // Then - assertions for result of method getDashboardProgressMetrics(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getDashboardProgressMetrics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getDashboardProgressMetrics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetDashboardProgressMetrics4() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<DashboardProgressMetrics> loader = mock(Supplier.class);
        DashboardProgressMetrics result = underTest.getDashboardProgressMetrics(loader);

        // Then - assertions for result of method getDashboardProgressMetrics(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getNewUserProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getNewUserProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetNewUserProgression() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat getResult = mock(ProgressStat.class);
        when(loader.get()).thenReturn(getResult);
        ProgressStat result = underTest.getNewUserProgression(loader);

        // Then - assertions for result of method getNewUserProgression(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getNewUserProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getNewUserProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetNewUserProgression2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String newUserProgressionDayKeyValue = "newUserProgressionDayKeyValue"; // UTA: configured value
        setPrivateField(underTest, DashboardCacheRegistry.class, "newUserProgressionDayKey", newUserProgressionDayKeyValue);

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat getResult = mock(ProgressStat.class);
        when(loader.get()).thenReturn(getResult);
        ProgressStat result = underTest.getNewUserProgression(loader);

        // Then - assertions for result of method getNewUserProgression(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getNewUserProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getNewUserProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetNewUserProgression3() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String newUserProgressionDayKeyValue = "newUserProgressionDayKeyValue"; // UTA: default value
        setPrivateField(underTest, DashboardCacheRegistry.class, "newUserProgressionDayKey", newUserProgressionDayKeyValue);

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat result = underTest.getNewUserProgression(loader);

        // Then - assertions for result of method getNewUserProgression(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getNewUserProgression(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getNewUserProgression(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetNewUserProgression4() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<ProgressStat> loader = mock(Supplier.class);
        ProgressStat result = underTest.getNewUserProgression(loader);

        // Then - assertions for result of method getNewUserProgression(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getOtherParasoftLatest(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getOtherParasoftLatest(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetOtherParasoftLatest() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<OtherParasoftEntry>> loader = mock(Supplier.class);
        List<OtherParasoftEntry> getResult = new ArrayList<OtherParasoftEntry>(); // UTA: default value
        OtherParasoftEntry item = mock(OtherParasoftEntry.class);
        getResult.add(item);
        doReturn(getResult).when(loader).get();
        List<OtherParasoftEntry> result = underTest.getOtherParasoftLatest(loader);

        // Then - assertions for result of method getOtherParasoftLatest(Supplier)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getOtherParasoftLatest(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getOtherParasoftLatest(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetOtherParasoftLatest2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<OtherParasoftEntry>> loader = mock(Supplier.class);
        List<OtherParasoftEntry> result = underTest.getOtherParasoftLatest(loader);

        // Then - assertions for result of method getOtherParasoftLatest(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionOverview(String, Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getSessionOverview(String, Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetSessionOverview() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        String key = "key"; // UTA: default value
        Supplier<SessionOverview> loader = mock(Supplier.class);
        SessionOverview result = underTest.getSessionOverview(key, loader);

        // Then - assertions for result of method getSessionOverview(String, Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionOverview(String, Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getSessionOverview(String, Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetSessionOverview2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        String key = "key"; // UTA: default value
        Supplier<SessionOverview> loader = mock(Supplier.class);
        SessionOverview getResult = mock(SessionOverview.class);
        when(loader.get()).thenReturn(getResult);
        SessionOverview result = underTest.getSessionOverview(key, loader);

        // Then - assertions for result of method getSessionOverview(String, Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getTermSummary(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTermSummary(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTermSummary() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<TermSummary> loader = mock(Supplier.class);
        TermSummary getResult = mock(TermSummary.class);
        when(loader.get()).thenReturn(getResult);
        TermSummary result = underTest.getTermSummary(loader);

        // Then - assertions for result of method getTermSummary(Supplier)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getTermSummary(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTermSummary(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTermSummary2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<TermSummary> loader = mock(Supplier.class);
        TermSummary result = underTest.getTermSummary(loader);

        // Then - assertions for result of method getTermSummary(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getTopTopics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTopTopics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTopTopics() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<TopTopic>> loader = mock(Supplier.class);
        List<TopTopic> getResult = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        getResult.add(item);
        doReturn(getResult).when(loader).get();
        List<TopTopic> result = underTest.getTopTopics(loader);

        // Then - assertions for result of method getTopTopics(Supplier)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getTopTopics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTopTopics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTopTopics2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String topTopicsDayKeyValue = "topTopicsDayKeyValue"; // UTA: configured value
        setPrivateField(underTest, DashboardCacheRegistry.class, "topTopicsDayKey", topTopicsDayKeyValue);

        // When
        Supplier<List<TopTopic>> loader = mock(Supplier.class);
        List<TopTopic> getResult = new ArrayList<TopTopic>(); // UTA: default value
        TopTopic item = mock(TopTopic.class);
        getResult.add(item);
        doReturn(getResult).when(loader).get();
        List<TopTopic> result = underTest.getTopTopics(loader);

        // Then - assertions for result of method getTopTopics(Supplier)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getTopTopics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTopTopics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTopTopics3() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String topTopicsDayKeyValue = "topTopicsDayKeyValue"; // UTA: default value
        setPrivateField(underTest, DashboardCacheRegistry.class, "topTopicsDayKey", topTopicsDayKeyValue);

        // When
        Supplier<List<TopTopic>> loader = mock(Supplier.class);
        List<TopTopic> result = underTest.getTopTopics(loader);

        // Then - assertions for result of method getTopTopics(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getTopTopics(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getTopTopics(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetTopTopics4() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<TopTopic>> loader = mock(Supplier.class);
        List<TopTopic> result = underTest.getTopTopics(loader);

        // Then - assertions for result of method getTopTopics(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetStats(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getWidgetStats(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetStats() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<WidgetStat>> loader = mock(Supplier.class);
        List<WidgetStat> getResult = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        getResult.add(item);
        doReturn(getResult).when(loader).get();
        List<WidgetStat> result = underTest.getWidgetStats(loader);

        // Then - assertions for result of method getWidgetStats(Supplier)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetStats(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getWidgetStats(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetStats2() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String widgetStatsDayKeyValue = "widgetStatsDayKeyValue"; // UTA: configured value
        setPrivateField(underTest, DashboardCacheRegistry.class, "widgetStatsDayKey", widgetStatsDayKeyValue);

        // When
        Supplier<List<WidgetStat>> loader = mock(Supplier.class);
        List<WidgetStat> getResult = new ArrayList<WidgetStat>(); // UTA: default value
        WidgetStat item = mock(WidgetStat.class);
        getResult.add(item);
        doReturn(getResult).when(loader).get();
        List<WidgetStat> result = underTest.getWidgetStats(loader);

        // Then - assertions for result of method getWidgetStats(Supplier)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetStats(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getWidgetStats(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetStats3() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();
        String widgetStatsDayKeyValue = "widgetStatsDayKeyValue"; // UTA: default value
        setPrivateField(underTest, DashboardCacheRegistry.class, "widgetStatsDayKey", widgetStatsDayKeyValue);

        // When
        Supplier<List<WidgetStat>> loader = mock(Supplier.class);
        List<WidgetStat> result = underTest.getWidgetStats(loader);

        // Then - assertions for result of method getWidgetStats(Supplier)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetStats(Supplier)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardCacheRegistry#getWidgetStats(Supplier)
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetStats4() throws Throwable
    {
        // Given
        DashboardCacheRegistry underTest = new DashboardCacheRegistry();

        // When
        Supplier<List<WidgetStat>> loader = mock(Supplier.class);
        List<WidgetStat> result = underTest.getWidgetStats(loader);

        // Then - assertions for result of method getWidgetStats(Supplier)
        assertNull(result);

    }
}
