package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

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
        JsonObject o = null; // UTA: configured value
        SelectedEntry result = SelectedEntry.fromJson(o);

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
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson3() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = null; // UTA: configured value
        when(o.get(nullable(Object.class))).thenReturn(getResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson4() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = null; // UTA: configured value
        when(o.get(nullable(Object.class))).thenReturn(getResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson5() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson6() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = "getStringResult"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson7() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson8() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = "getStringResult"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson9() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson10() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = true; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson11() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = true; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson12() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = false; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson13() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = false; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson14() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = null; // UTA: configured value
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = "getStringResult"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson15() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = null; // UTA: configured value
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson16() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        JsonValue getResult4 = null; // UTA: configured value
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3, getResult4);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson17() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = "getStringResult"; // UTA: default value
        String getStringResult2 = "getStringResult2"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult, getStringResult2);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson18() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = "getStringResult"; // UTA: default value
        String getStringResult2 = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult, getStringResult2);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson19() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = null; // UTA: configured value
        String getStringResult2 = "getStringResult2"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult, getStringResult2);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson20() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = null; // UTA: configured value
        String getStringResult2 = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult, getStringResult2);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson21() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = "getStringResult"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson22() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson23() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        JsonValue getResult4 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3, getResult4);

        String getStringResult = "getStringResult"; // UTA: default value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson24() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        JsonValue getResult4 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3, getResult4);

        String getStringResult = null; // UTA: configured value
        when(o.getString(nullable(String.class), nullable(String.class))).thenReturn(getStringResult);
        SelectedEntry result = SelectedEntry.fromJson(o);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJson(JsonObject)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJson(JsonObject)
     * @author bmcmullin
     */
    @Test
    public void testFromJson25() throws Throwable
    {
        // When
        JsonObject o = mock(JsonObject.class);
        boolean containsKeyResult = false; // UTA: configured value
        boolean containsKeyResult2 = false; // UTA: configured value
        boolean containsKeyResult3 = false; // UTA: configured value
        boolean containsKeyResult4 = true; // UTA: configured value
        boolean containsKeyResult5 = true; // UTA: configured value
        when(o.containsKey(nullable(Object.class))).thenReturn(containsKeyResult, containsKeyResult2, containsKeyResult3, containsKeyResult4, containsKeyResult5);

        JsonValue getResult = mock(JsonValue.class);
        JsonValue getResult2 = mock(JsonValue.class);
        JsonValue getResult3 = mock(JsonValue.class);
        JsonValue getResult4 = mock(JsonValue.class);
        when(o.get(nullable(Object.class))).thenReturn(getResult, getResult2, getResult3, getResult4);
        SelectedEntry result = SelectedEntry.fromJson(o);

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
        boolean isEmptyResult = true; // UTA: configured value
        when(arr.isEmpty()).thenReturn(isEmptyResult);
        int maxItems = 1; // UTA: default value
        List<SelectedEntry> result = SelectedEntry.fromJsonArray(arr, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJsonArray(JsonArray, int)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJsonArray(JsonArray, int)
     * @author bmcmullin
     */
    @Test
    public void testFromJsonArray3() throws Throwable
    {
        // When
        JsonArray arr = mock(JsonArray.class);
        boolean isEmptyResult = false; // UTA: configured value
        when(arr.isEmpty()).thenReturn(isEmptyResult);
        int maxItems = 0; // UTA: configured value
        List<SelectedEntry> result = SelectedEntry.fromJsonArray(arr, maxItems);

    }

    /**
     * Parasoft Jtest UTA: Test for fromJsonArray(JsonArray, int)
     *
     * @see com.sim.chatserver.model.SelectedEntry#fromJsonArray(JsonArray, int)
     * @author bmcmullin
     */
    @Test
    public void testFromJsonArray4() throws Throwable
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

    }

    /**
     * Parasoft Jtest UTA: Test for toJsonArray(List)
     *
     * @see com.sim.chatserver.model.SelectedEntry#toJsonArray(List)
     * @author bmcmullin
     */
    @Test
    public void testToJsonArray4() throws Throwable
    {
        // When
        List<SelectedEntry> entries = new ArrayList<SelectedEntry>(); // UTA: default value
        SelectedEntry item = mock(SelectedEntry.class);
        JsonObject toJsonResult = mock(JsonObject.class);
        when(item.toJson()).thenReturn(toJsonResult);
        entries.add(item);
        SelectedEntry item2 = mock(SelectedEntry.class);
        JsonObject toJsonResult2 = mock(JsonObject.class);
        when(item2.toJson()).thenReturn(toJsonResult2);
        entries.add(item2);
        JsonArray result = SelectedEntry.toJsonArray(entries);

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

    }
}
