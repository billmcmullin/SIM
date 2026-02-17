package com.sim.chatserver.model;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for WidgetMapping
 *
 * @see com.sim.chatserver.model.WidgetMapping
 * @author bmcmullin
 */
public class WidgetMappingTest
{

    /**
     * Parasoft Jtest UTA: Test for getCategory()
     *
     * @see com.sim.chatserver.model.WidgetMapping#getCategory()
     * @author bmcmullin
     */
    @Test
    public void testGetCategory() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String result = underTest.getCategory();

        // Then - assertions for result of method getCategory()
        assertNull(result);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDisplayName()
     *
     * @see com.sim.chatserver.model.WidgetMapping#getDisplayName()
     * @author bmcmullin
     */
    @Test
    public void testGetDisplayName() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String result = underTest.getDisplayName();

        // Then - assertions for result of method getDisplayName()
        assertNull(result);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmbedUuid()
     *
     * @see com.sim.chatserver.model.WidgetMapping#getEmbedUuid()
     * @author bmcmullin
     */
    @Test
    public void testGetEmbedUuid() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String result = underTest.getEmbedUuid();

        // Then - assertions for result of method getEmbedUuid()
        assertNull(result);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getId()
     *
     * @see com.sim.chatserver.model.WidgetMapping#getId()
     * @author bmcmullin
     */
    @Test
    public void testGetId() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        Long result = underTest.getId();

        // Then - assertions for result of method getId()
        assertNull(result);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLastSyncAt()
     *
     * @see com.sim.chatserver.model.WidgetMapping#getLastSyncAt()
     * @author bmcmullin
     */
    @Test
    public void testGetLastSyncAt() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        OffsetDateTime result = underTest.getLastSyncAt();

        // Then - assertions for result of method getLastSyncAt()
        assertNull(result);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for prePersist()
     *
     * @see com.sim.chatserver.model.WidgetMapping#prePersist()
     * @author bmcmullin
     */
    @Test
    public void testPrePersist() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();
        OffsetDateTime createdAtValue = mock(OffsetDateTime.class);
        setPrivateField(underTest, WidgetMapping.class, "createdAt", createdAtValue);

        // When
        underTest.prePersist();

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Helper method to set private field createdAt
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
     * Parasoft Jtest UTA: Test for prePersist()
     *
     * @see com.sim.chatserver.model.WidgetMapping#prePersist()
     * @author bmcmullin
     */
    @Test
    public void testPrePersist2() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        underTest.prePersist();

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setCategory(String)
     *
     * @see com.sim.chatserver.model.WidgetMapping#setCategory(String)
     * @author bmcmullin
     */
    @Test
    public void testSetCategory() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String c = "c"; // UTA: default value
        underTest.setCategory(c);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertEquals("c", underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDisplayName(String)
     *
     * @see com.sim.chatserver.model.WidgetMapping#setDisplayName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDisplayName() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String s = "s"; // UTA: default value
        underTest.setDisplayName(s);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertEquals("s", underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setEmbedUuid(String)
     *
     * @see com.sim.chatserver.model.WidgetMapping#setEmbedUuid(String)
     * @author bmcmullin
     */
    @Test
    public void testSetEmbedUuid() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        String s = "s"; // UTA: default value
        underTest.setEmbedUuid(s);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertEquals("s", underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNull(underTest.getLastSyncAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setLastSyncAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.WidgetMapping#setLastSyncAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetLastSyncAt() throws Throwable
    {
        // Given
        WidgetMapping underTest = new WidgetMapping();

        // When
        OffsetDateTime t = mock(OffsetDateTime.class);
        underTest.setLastSyncAt(t);

        // Then - assertions for this instance of WidgetMapping
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getEmbedUuid());
        }, () -> {
            assertNull(underTest.getDisplayName());
        }, () -> {
            assertNull(underTest.getCategory());
        }, () -> {
            assertNotNull(underTest.getLastSyncAt());
        });

    }
}
