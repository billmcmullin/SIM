package com.sim.chatserver.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AttachmentSanitizer
 *
 * @see com.sim.chatserver.security.AttachmentSanitizer
 * @author bmcmullin
 */
public class AttachmentSanitizerTest
{

    /**
     * Parasoft Jtest UTA: Test for sanitize(JsonArray)
     *
     * @see com.sim.chatserver.security.AttachmentSanitizer#sanitize(JsonArray)
     * @author bmcmullin
     */
    @Test
    public void testSanitize() throws Throwable
    {
        // Given
        AttachmentSanitizer underTest = new AttachmentSanitizer();

        // When
        JsonArray rawAttachments = null; // UTA: configured value
        JsonArray result = underTest.sanitize(rawAttachments);

        // Then - assertions for result of method sanitize(JsonArray)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for sanitize(JsonArray)
     *
     * @see com.sim.chatserver.security.AttachmentSanitizer#sanitize(JsonArray)
     * @author bmcmullin
     */
    @Test
    public void testSanitize2() throws Throwable
    {
        // Given
        AttachmentSanitizer underTest = new AttachmentSanitizer();

        // When
        JsonArray rawAttachments = mock(JsonArray.class);
        boolean isEmptyResult = true; // UTA: configured value
        when(rawAttachments.isEmpty()).thenReturn(isEmptyResult);
        JsonArray result = underTest.sanitize(rawAttachments);

        // Then - assertions for result of method sanitize(JsonArray)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for sanitize(JsonArray)
     *
     * @see com.sim.chatserver.security.AttachmentSanitizer#sanitize(JsonArray)
     * @author bmcmullin
     */
    @Test
    public void testSanitize3() throws Throwable
    {
        // Given
        AttachmentSanitizer underTest = new AttachmentSanitizer();

        // When
        JsonArray rawAttachments = mock(JsonArray.class);
        boolean isEmptyResult = false; // UTA: configured value
        when(rawAttachments.isEmpty()).thenReturn(isEmptyResult);

        ArrayList iteratorResult_list = new ArrayList(); // UTA: default value
        JsonValue item = mock(JsonValue.class);
        iteratorResult_list.add(item);
        Iterator<JsonValue> iteratorResult = iteratorResult_list.iterator(); // UTA: default value
        doReturn(iteratorResult).when(rawAttachments).iterator();
        JsonArray result = underTest.sanitize(rawAttachments);

        // Then - assertions for result of method sanitize(JsonArray)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }

    /**
     * Parasoft Jtest UTA: Test for sanitize(JsonArray)
     *
     * @see com.sim.chatserver.security.AttachmentSanitizer#sanitize(JsonArray)
     * @author bmcmullin
     */
    @Test
    public void testSanitize4() throws Throwable
    {
        // Given
        int maxAttachmentCount = 1; // UTA: default value
        int maxNameChars = 1; // UTA: default value
        int maxMimeChars = 1; // UTA: default value
        int maxContentChars = 1; // UTA: default value
        AttachmentSanitizer underTest = new AttachmentSanitizer(maxAttachmentCount, maxNameChars, maxMimeChars, maxContentChars);

        // When
        JsonArray rawAttachments = mock(JsonArray.class);
        boolean isEmptyResult = false; // UTA: configured value
        when(rawAttachments.isEmpty()).thenReturn(isEmptyResult);

        ArrayList iteratorResult_list = new ArrayList(); // UTA: default value
        JsonValue item = mock(JsonValue.class);
        iteratorResult_list.add(item);
        Iterator<JsonValue> iteratorResult = iteratorResult_list.iterator(); // UTA: default value
        doReturn(iteratorResult).when(rawAttachments).iterator();
        JsonArray result = underTest.sanitize(rawAttachments);

        // Then - assertions for result of method sanitize(JsonArray)
        assertNotNull(result);
        assertTrue(result instanceof Collection);
        assertEquals(0, ((Collection<?>) result).size());

    }
}
