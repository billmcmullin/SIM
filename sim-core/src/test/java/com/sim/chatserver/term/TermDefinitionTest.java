package com.sim.chatserver.term;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Parasoft Jtest UTA: Test class for TermDefinition
 *
 * @see com.sim.chatserver.term.TermDefinition
 * @author bmcmullin
 */
public class TermDefinitionTest
{

    /**
     * Parasoft Jtest UTA: Test for getDescription()
     *
     * @see com.sim.chatserver.term.TermDefinition#getDescription()
     * @author bmcmullin
     */
    @Test
    public void testGetDescription() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        String result = underTest.getDescription();

        // Then - assertions for result of method getDescription()
        assertEquals("description", result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("matchPattern", underTest.getMatchPattern());
        }, () -> {
            assertEquals("matchType", underTest.getMatchType());
        }, () -> {
            assertFalse(underTest.isSystemFlag());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getId()
     *
     * @see com.sim.chatserver.term.TermDefinition#getId()
     * @author bmcmullin
     */
    @Test
    public void testGetId() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        Long result = underTest.getId();

        // Then - assertions for result of method getId()
        assertEquals(1L, result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("description", underTest.getDescription());
        }, () -> {
            assertEquals("matchPattern", underTest.getMatchPattern());
        }, () -> {
            assertEquals("matchType", underTest.getMatchType());
        }, () -> {
            assertFalse(underTest.isSystemFlag());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMatchPattern()
     *
     * @see com.sim.chatserver.term.TermDefinition#getMatchPattern()
     * @author bmcmullin
     */
    @Test
    public void testGetMatchPattern() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        String result = underTest.getMatchPattern();

        // Then - assertions for result of method getMatchPattern()
        assertEquals("matchPattern", result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("description", underTest.getDescription());
        }, () -> {
            assertEquals("matchType", underTest.getMatchType());
        }, () -> {
            assertFalse(underTest.isSystemFlag());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMatchType()
     *
     * @see com.sim.chatserver.term.TermDefinition#getMatchType()
     * @author bmcmullin
     */
    @Test
    public void testGetMatchType() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        String result = underTest.getMatchType();

        // Then - assertions for result of method getMatchType()
        assertEquals("matchType", result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("description", underTest.getDescription());
        }, () -> {
            assertEquals("matchPattern", underTest.getMatchPattern());
        }, () -> {
            assertFalse(underTest.isSystemFlag());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getName()
     *
     * @see com.sim.chatserver.term.TermDefinition#getName()
     * @author bmcmullin
     */
    @Test
    public void testGetName() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        String result = underTest.getName();

        // Then - assertions for result of method getName()
        assertEquals("name", result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertEquals("description", underTest.getDescription());
        }, () -> {
            assertEquals("matchPattern", underTest.getMatchPattern());
        }, () -> {
            assertEquals("matchType", underTest.getMatchType());
        }, () -> {
            assertFalse(underTest.isSystemFlag());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isSystemFlag()
     *
     * @see com.sim.chatserver.term.TermDefinition#isSystemFlag()
     * @author bmcmullin
     */
    @Test
    public void testIsSystemFlag() throws Throwable
    {
        // Given
        Long id = 1L; // UTA: default value
        String name = "name"; // UTA: default value
        String description = "description"; // UTA: default value
        String matchPattern = "matchPattern"; // UTA: default value
        String matchType = "matchType"; // UTA: default value
        boolean systemFlag = false; // UTA: default value
        TermDefinition underTest = new TermDefinition(id, name, description, matchPattern, matchType, systemFlag);

        // When
        boolean result = underTest.isSystemFlag();

        // Then - assertions for result of method isSystemFlag()
        assertFalse(result);

        // Then - assertions for this instance of TermDefinition
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("description", underTest.getDescription());
        }, () -> {
            assertEquals("matchPattern", underTest.getMatchPattern());
        }, () -> {
            assertEquals("matchType", underTest.getMatchType());
        });

    }

    @Test
    public void testSerializationGuards_throwNotSerializableException() throws Throwable
    {
        TermDefinition underTest = new TermDefinition(1L, "name", "desc", "pattern", "type", true);

        Method readObject = TermDefinition.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
        readObject.setAccessible(true);
        InvocationTargetException readEx = assertThrows(InvocationTargetException.class,
                () -> readObject.invoke(underTest, new Object[]{null}));
        assertEquals(NotSerializableException.class, readEx.getCause().getClass());

        Method writeObject = TermDefinition.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        writeObject.setAccessible(true);
        InvocationTargetException writeEx = assertThrows(InvocationTargetException.class,
                () -> writeObject.invoke(underTest, new Object[]{null}));
        assertEquals(NotSerializableException.class, writeEx.getCause().getClass());
    }
}
