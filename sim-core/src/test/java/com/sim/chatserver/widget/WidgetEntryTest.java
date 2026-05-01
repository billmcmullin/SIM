package com.sim.chatserver.widget;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for WidgetEntry
 *
 * @see com.sim.chatserver.widget.WidgetEntry
 * @author bmcmullin
 */
public class WidgetEntryTest
{

    /**
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.widget.WidgetEntry#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        int id = 1; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String displayName = "displayName"; // UTA: default value
        Instant createdAt = mock(Instant.class);
        WidgetEntry underTest = new WidgetEntry(id, widgetId, displayName, createdAt);

        // When
        Instant result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertNotNull(result);

        // Then - assertions for this instance of WidgetEntry
        assertAll(() -> {
            assertEquals(1, underTest.getId());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("displayName", underTest.getDisplayName());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDisplayName()
     *
     * @see com.sim.chatserver.widget.WidgetEntry#getDisplayName()
     * @author bmcmullin
     */
    @Test
    public void testGetDisplayName() throws Throwable
    {
        // Given
        int id = 1; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String displayName = "displayName"; // UTA: default value
        Instant createdAt = mock(Instant.class);
        WidgetEntry underTest = new WidgetEntry(id, widgetId, displayName, createdAt);

        // When
        String result = underTest.getDisplayName();

        // Then - assertions for result of method getDisplayName()
        assertEquals("displayName", result);

        // Then - assertions for this instance of WidgetEntry
        assertAll(() -> {
            assertEquals(1, underTest.getId());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getId()
     *
     * @see com.sim.chatserver.widget.WidgetEntry#getId()
     * @author bmcmullin
     */
    @Test
    public void testGetId() throws Throwable
    {
        // Given
        int id = 1; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String displayName = "displayName"; // UTA: default value
        Instant createdAt = mock(Instant.class);
        WidgetEntry underTest = new WidgetEntry(id, widgetId, displayName, createdAt);

        // When
        int result = underTest.getId();

        // Then - assertions for result of method getId()
        assertEquals(1, result);

        // Then - assertions for this instance of WidgetEntry
        assertAll(() -> {
            assertEquals("displayName", underTest.getDisplayName());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetId()
     *
     * @see com.sim.chatserver.widget.WidgetEntry#getWidgetId()
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetId() throws Throwable
    {
        // Given
        int id = 1; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String displayName = "displayName"; // UTA: default value
        Instant createdAt = mock(Instant.class);
        WidgetEntry underTest = new WidgetEntry(id, widgetId, displayName, createdAt);

        // When
        String result = underTest.getWidgetId();

        // Then - assertions for result of method getWidgetId()
        assertEquals("widgetId", result);

        // Then - assertions for this instance of WidgetEntry
        assertAll(() -> {
            assertEquals(1, underTest.getId());
        }, () -> {
            assertEquals("displayName", underTest.getDisplayName());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }
}
