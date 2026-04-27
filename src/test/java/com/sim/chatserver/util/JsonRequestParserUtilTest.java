package com.sim.chatserver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for JsonRequestParserUtil
 *
 * @see com.sim.chatserver.util.JsonRequestParserUtil
 * @author bmcmullin
 */
public class JsonRequestParserUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

        // Then - assertions for result of method getArray(JsonObject, String)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray2() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

        // Then - assertions for result of method getArray(JsonObject, String)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

        // Then - assertions for result of method getBoolean(JsonObject, String, boolean)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean2() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

        // Then - assertions for result of method getBoolean(JsonObject, String, boolean)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean3() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = mock(JsonString.class);
        ValueType getValueTypeResult = ValueType.ARRAY; // UTA: default value
        when(getResult.getValueType()).thenReturn(getValueTypeResult);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

        // Then - assertions for result of method getBoolean(JsonObject, String, boolean)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for getInt(JsonObject, String, int, int, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getInt(JsonObject, String, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testGetInt() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        int defaultValue = 1; // UTA: default value
        int min = 1; // UTA: default value
        int max = 1; // UTA: default value
        int result = JsonRequestParserUtil.getInt(obj, key, defaultValue, min, max);

        // Then - assertions for result of method getInt(JsonObject, String, int, int, int)
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for getInt(JsonObject, String, int, int, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getInt(JsonObject, String, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testGetInt2() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int defaultValue = 1; // UTA: default value
        int min = 1; // UTA: default value
        int max = 1; // UTA: default value
        int result = JsonRequestParserUtil.getInt(obj, key, defaultValue, min, max);

        // Then - assertions for result of method getInt(JsonObject, String, int, int, int)
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for getInt(JsonObject, String, int, int, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getInt(JsonObject, String, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testGetInt3() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = mock(JsonString.class);
        ValueType getValueTypeResult = ValueType.ARRAY; // UTA: default value
        when(getResult.getValueType()).thenReturn(getValueTypeResult);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int defaultValue = 1; // UTA: default value
        int min = 1; // UTA: default value
        int max = 1; // UTA: default value
        int result = JsonRequestParserUtil.getInt(obj, key, defaultValue, min, max);

        // Then - assertions for result of method getInt(JsonObject, String, int, int, int)
        assertEquals(1, result);

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

        // Then - assertions for result of method getObjectArray(JsonObject, String, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

        // Then - assertions for result of method getString(JsonObject, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString2() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

        // Then - assertions for result of method getString(JsonObject, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString3() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

        // Then - assertions for result of method getString(JsonObject, String, int)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString4() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = mock(JsonValue.class);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

        // Then - assertions for result of method getString(JsonObject, String, int)
        assertEquals("M", result);

    }

    /**
     * Parasoft Jtest UTA: Test for parseObject(HttpServletRequest)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#parseObject(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testParseObject() throws Throwable
    {
        // When
        HttpServletRequest req = null; // UTA: configured value
        JsonObject result = JsonRequestParserUtil.parseObject(req);

        // Then - assertions for result of method parseObject(HttpServletRequest)
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for parseObject(HttpServletRequest)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#parseObject(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    public void testParseObject2() throws Throwable
    {
        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        JsonObject result = JsonRequestParserUtil.parseObject(req);

        // Then - assertions for result of method parseObject(HttpServletRequest)
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());

    }

}
