package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SelectedEntry
 *
 * @see com.sim.chatserver.model.SelectedEntry
 * @author bmcmullin
 */
public class SelectedEntryTest
{

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.model.SelectedEntry#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        Object o2 = null; // UTA: configured value
        boolean result = underTest.equals(o2);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.model.SelectedEntry#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals2() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        SelectedEntry o2 = mock(SelectedEntry.class);
        boolean result = underTest.equals(o2);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);
        SelectedEntry result = SelectedEntry.fromJson(o);

        // Then - assertions for result of method fromJson(JsonObject)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.getChatId());
        }, () -> {
            assertEquals("", result.getPrompt());
        }, () -> {
            assertEquals("", result.getResponse());
        }, () -> {
            assertEquals("", result.getCreatedAt());
        }, () -> {
            assertEquals("", result.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson2() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

        // Then - assertions for result of method fromJson(JsonObject)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("Mock for JsonValue, hashCode: 1772319741", result.getChatId());
        }, () -> {
            assertEquals("", result.getPrompt());
        }, () -> {
            assertEquals("", result.getResponse());
        }, () -> {
            assertEquals("", result.getCreatedAt());
        }, () -> {
            assertEquals("", result.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromJsonArray(JsonArray, int)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJsonArray(JsonArray, int)
     * @author bmcmullin
     */
    @Test
    public void testFromJsonArray() throws Throwable
    {
        // When
        JsonArray arr = null; // UTA: configured value
        int maxItems = 1; // UTA: default value
        List<SelectedEntry> result = SelectedEntry.fromJsonArray(arr, maxItems);

        // Then - assertions for result of method fromJsonArray(JsonArray, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for fromJsonArray(JsonArray, int)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJsonArray(JsonArray, int)
     * @author bmcmullin
     */
    @Test
    public void testFromJsonArray2() throws Throwable
    {
        // When
        JsonArray arr = mock(JsonArray.class);
        boolean isEmptyResult = false; // UTA: configured value
        when(arr.isEmpty()).thenReturn(isEmptyResult);

        ArrayList iteratorResult_list = new ArrayList(); // UTA: default value
        JsonValue item = mock(JsonValue.class);
        iteratorResult_list.add(item);
        Iterator<JsonValue> iteratorResult = iteratorResult_list.iterator(); // UTA: default value
        doReturn(iteratorResult).when(arr).iterator();
        int maxItems = 1; // UTA: configured value
        List<SelectedEntry> result = SelectedEntry.fromJsonArray(arr, maxItems);

        // Then - assertions for result of method fromJsonArray(JsonArray, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getChatId()
     *
     * @see com.sim.chatserver.model.SelectedEntry#getChatId()
     * @author bmcmullin
     */
    @Test
    public void testGetChatId() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.getChatId();

        // Then - assertions for result of method getChatId()
        assertEquals("", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.model.SelectedEntry#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertEquals("", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPrompt()
     *
     * @see com.sim.chatserver.model.SelectedEntry#getPrompt()
     * @author bmcmullin
     */
    @Test
    public void testGetPrompt() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.getPrompt();

        // Then - assertions for result of method getPrompt()
        assertEquals("", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getResponse()
     *
     * @see com.sim.chatserver.model.SelectedEntry#getResponse()
     * @author bmcmullin
     */
    @Test
    public void testGetResponse() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.getResponse();

        // Then - assertions for result of method getResponse()
        assertEquals("", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.model.SelectedEntry#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertEquals("", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hashCode()
     *
     * @see com.sim.chatserver.model.SelectedEntry#hashCode()
     * @author bmcmullin
     */
    @Test
    public void testHashCode() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        int result = underTest.hashCode();

        // Then - assertions for result of method hashCode()
        // assertEquals(1, result);// UTA: Expected value may be unstable

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJson()
     *
     * @see com.sim.chatserver.model.SelectedEntry#toJson()
     * @author bmcmullin
     */
    @Test
    public void testToJson() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        JsonObject result = underTest.toJson();

        // Then - assertions for result of method toJson()
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(5, ((Map<?, ?>) result).size());

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toJsonArray(List)
     *
     * @see com.sim.chatserver.model.SelectedEntry#toJsonArray(List)
     * @author bmcmullin
     */
    @Test
    public void testToJsonArray() throws Throwable
    {
        // When
        List<SelectedEntry> entries = null; // UTA: configured value
        JsonArray result = SelectedEntry.toJsonArray(entries);

        // Then - assertions for result of method toJsonArray(List)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for toJsonArray(List)
     *
     * @see com.sim.chatserver.model.SelectedEntry#toJsonArray(List)
     * @author bmcmullin
     */
    @Test
    public void testToJsonArray2() throws Throwable
    {
        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        JsonArray result = SelectedEntry.toJsonArray(entries);

        // Then - assertions for result of method toJsonArray(List)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for toJsonArray(List)
     *
     * @see com.sim.chatserver.model.SelectedEntry#toJsonArray(List)
     * @author bmcmullin
     */
    @Test
    public void testToJsonArray3() throws Throwable
    {
        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        JsonObject toJsonResult = mock(JsonObject.class);
        when(item.toJson()).thenReturn(toJsonResult);
        entries.add(item);
        JsonArray result = SelectedEntry.toJsonArray(entries);

        // Then - assertions for result of method toJsonArray(List)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(1, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.model.SelectedEntry#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        JsonObject o = null; // UTA: configured value
        SelectedEntry underTest = SelectedEntry.fromJson(o);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("SelectedEntry{chatId='', createdAt='', sessionId=''}", result);

        // Then - assertions for this instance of SelectedEntry
        assertAll(() -> {
            assertEquals("", underTest.getChatId());
        }, () -> {
            assertEquals("", underTest.getPrompt());
        }, () -> {
            assertEquals("", underTest.getResponse());
        }, () -> {
            assertEquals("", underTest.getCreatedAt());
        }, () -> {
            assertEquals("", underTest.getSessionId());
        });

    }
}
