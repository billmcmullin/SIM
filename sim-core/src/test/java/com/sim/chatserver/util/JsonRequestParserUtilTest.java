package com.sim.chatserver.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.servlet.http.HttpServletRequest;

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
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray3() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        String key = null; // UTA: configured value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray4() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray5() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray6() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonArray getJsonArrayResult = mock(JsonArray.class);
        when(obj.getJsonArray(nullable(String.class))).thenReturn(getJsonArrayResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

    }

    /**
     * Parasoft Jtest UTA: Test for getArray(JsonObject, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getArray(JsonObject, String)
     * @author bmcmullin
     */
    @Test
    public void testGetArray7() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonArray getJsonArrayResult = null; // UTA: configured value
        when(obj.getJsonArray(nullable(String.class))).thenReturn(getJsonArrayResult);
        String key = "key"; // UTA: default value
        JsonArray result = JsonRequestParserUtil.getArray(obj, key);

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
        String key = null; // UTA: configured value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

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
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean4() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean5() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for getBoolean(JsonObject, String, boolean)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getBoolean(JsonObject, String, boolean)
     * @author bmcmullin
     */
    @Test
    public void testGetBoolean6() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        String key = "key"; // UTA: default value
        boolean defaultValue = false; // UTA: default value
        boolean result = JsonRequestParserUtil.getBoolean(obj, key, defaultValue);

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
        String key = null; // UTA: configured value
        int defaultValue = 1; // UTA: default value
        int min = 1; // UTA: default value
        int max = 1; // UTA: default value
        int result = JsonRequestParserUtil.getInt(obj, key, defaultValue, min, max);

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
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        int defaultValue = 1; // UTA: default value
        int min = 1; // UTA: default value
        int max = 1; // UTA: default value
        int result = JsonRequestParserUtil.getInt(obj, key, defaultValue, min, max);

    }

    /**
     * Parasoft Jtest UTA: Test for getInt(JsonObject, String, int, int, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getInt(JsonObject, String, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testGetInt4() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for getInt(JsonObject, String, int, int, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getInt(JsonObject, String, int, int, int)
     * @author bmcmullin
     */
    @Test
    public void testGetInt5() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray2() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray3() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        String key = null; // UTA: configured value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray4() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray5() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonArray getJsonArrayResult = mock(JsonArray.class);
        ArrayList iteratorResult_list = new ArrayList(); // UTA: default value
        JsonValue item = mock(JsonValue.class);
        iteratorResult_list.add(item);
        Iterator<JsonValue> iteratorResult = iteratorResult_list.iterator(); // UTA: default value
        doReturn(iteratorResult).when(getJsonArrayResult).iterator();
        when(obj.getJsonArray(nullable(String.class))).thenReturn(getJsonArrayResult);
        String key = "key"; // UTA: default value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for getObjectArray(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getObjectArray(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetObjectArray6() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        int maxItems = 1; // UTA: default value
        List<JsonObject> result = JsonRequestParserUtil.getObjectArray(obj, key, maxItems);

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
        String key = null; // UTA: configured value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

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
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

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

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString5() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString6() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString7() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        String key = null; // UTA: configured value
        int maxChars = 1; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString8() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString9() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString10() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = mock(JsonString.class);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 1; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString11() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonValue getResult = mock(JsonValue.class);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, int)
     * @author bmcmullin
     */
    @Test
    public void testGetString12() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = mock(JsonString.class);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        int maxChars = 0; // UTA: configured value
        String result = JsonRequestParserUtil.getString(obj, key, maxChars);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString13() throws Throwable
    {
        // When
        JsonObject obj = null; // UTA: configured value
        String key = "key"; // UTA: default value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString14() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        String key = null; // UTA: configured value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString15() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString16() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = null; // UTA: configured value
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString17() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);
        String key = "key"; // UTA: default value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

    }

    /**
     * Parasoft Jtest UTA: Test for getString(JsonObject, String, String)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#getString(JsonObject, String, String)
     * @author bmcmullin
     */
    @Test
    public void testGetString18() throws Throwable
    {
        // When
        JsonObject obj = mock(JsonObject.class);
        boolean containsKeyResult = true; // UTA: configured value
        when(obj.containsKey(nullable(Object.class))).thenReturn(containsKeyResult);

        JsonString getResult = mock(JsonString.class);
        when(obj.get(nullable(Object.class))).thenReturn(getResult);
        String key = "key"; // UTA: default value
        String defaultValue = "defaultValue"; // UTA: default value
        String result = JsonRequestParserUtil.getString(obj, key, defaultValue);

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

    }

    /**
     * Parasoft Jtest UTA: Test for parseObject(HttpServletRequest, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#parseObject(HttpServletRequest, int)
     * @author bmcmullin
     */
    @Test
    public void testParseObject3() throws Throwable
    {
        // When
        HttpServletRequest req = null; // UTA: configured value
        int maxBodyBytes = 1; // UTA: default value
        JsonObject result = JsonRequestParserUtil.parseObject(req, maxBodyBytes);

    }

    /**
     * Parasoft Jtest UTA: Test for parseObject(HttpServletRequest, int)
     *
     * @see com.sim.chatserver.util.JsonRequestParserUtil#parseObject(HttpServletRequest, int)
     * @author bmcmullin
     */
    @Test
    public void testParseObject4() throws Throwable
    {
        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        int maxBodyBytes = 1; // UTA: default value
        JsonObject result = JsonRequestParserUtil.parseObject(req, maxBodyBytes);

    }
}
