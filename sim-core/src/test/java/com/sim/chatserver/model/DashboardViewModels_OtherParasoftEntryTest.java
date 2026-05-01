package com.sim.chatserver.model;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for OtherParasoftEntry
 *
 * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry
 * @author bmcmullin
 */
public class DashboardViewModels_OtherParasoftEntryTest
{

    /**
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String widgetName = "widgetName"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        OtherParasoftEntry underTest = new OtherParasoftEntry(widgetId, widgetName, prompt, sessionId, createdAt);

        // When
        Timestamp result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertNotNull(result);

        // Then - assertions for this instance of DashboardViewModels.OtherParasoftEntry
        assertAll(() -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("widgetName", underTest.getWidgetName());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPrompt()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry#getPrompt()
     * @author bmcmullin
     */
    @Test
    public void testGetPrompt() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String widgetName = "widgetName"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        OtherParasoftEntry underTest = new OtherParasoftEntry(widgetId, widgetName, prompt, sessionId, createdAt);

        // When
        String result = underTest.getPrompt();

        // Then - assertions for result of method getPrompt()
        assertEquals("prompt", result);

        // Then - assertions for this instance of DashboardViewModels.OtherParasoftEntry
        assertAll(() -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("widgetName", underTest.getWidgetName());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String widgetName = "widgetName"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        OtherParasoftEntry underTest = new OtherParasoftEntry(widgetId, widgetName, prompt, sessionId, createdAt);

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertEquals("sessionId", result);

        // Then - assertions for this instance of DashboardViewModels.OtherParasoftEntry
        assertAll(() -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("widgetName", underTest.getWidgetName());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetId()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry#getWidgetId()
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetId() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String widgetName = "widgetName"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        OtherParasoftEntry underTest = new OtherParasoftEntry(widgetId, widgetName, prompt, sessionId, createdAt);

        // When
        String result = underTest.getWidgetId();

        // Then - assertions for result of method getWidgetId()
        assertEquals("widgetId", result);

        // Then - assertions for this instance of DashboardViewModels.OtherParasoftEntry
        assertAll(() -> {
            assertEquals("widgetName", underTest.getWidgetName());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetName()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry#getWidgetName()
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetName() throws Throwable
    {
        // Given
        String widgetId = "widgetId"; // UTA: default value
        String widgetName = "widgetName"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String sessionId = "sessionId"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        OtherParasoftEntry underTest = new OtherParasoftEntry(widgetId, widgetName, prompt, sessionId, createdAt);

        // When
        String result = underTest.getWidgetName();

        // Then - assertions for result of method getWidgetName()
        assertEquals("widgetName", result);

        // Then - assertions for this instance of DashboardViewModels.OtherParasoftEntry
        assertAll(() -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }
}
