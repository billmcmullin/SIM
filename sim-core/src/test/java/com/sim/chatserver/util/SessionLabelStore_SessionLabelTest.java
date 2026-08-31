package com.sim.chatserver.util;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.util.SessionLabelStore.SessionLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Parasoft Jtest UTA: Test class for SessionLabel
 *
 * @see com.sim.chatserver.util.SessionLabelStore.SessionLabel
 * @author bmcmullin
 */
public class SessionLabelStore_SessionLabelTest
{

    /**
     * Parasoft Jtest UTA: Test for getDisplayName()
     *
     * @see com.sim.chatserver.util.SessionLabelStore.SessionLabel#getDisplayName()
     * @author bmcmullin
     */
    @Test
    public void testGetDisplayName() throws Throwable
    {
        // Given
        String displayName = "displayName"; // UTA: default value
        String email = "email"; // UTA: default value
        SessionLabel underTest = newSessionLabel(displayName, email);

        // When
        String result = underTest.getDisplayName();

        // Then - assertions for result of method getDisplayName()
        assertEquals("displayName", result);

        // Then - assertions for this instance of SessionLabelStore.SessionLabel
        assertEquals("email", underTest.getEmail());

    }

    /**
     * Parasoft Jtest UTA: Test for getEmail()
     *
     * @see com.sim.chatserver.util.SessionLabelStore.SessionLabel#getEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetEmail() throws Throwable
    {
        // Given
        String displayName = "displayName"; // UTA: default value
        String email = "email"; // UTA: default value
        SessionLabel underTest = newSessionLabel(displayName, email);

        // When
        String result = underTest.getEmail();

        // Then - assertions for result of method getEmail()
        assertEquals("email", result);

        // Then - assertions for this instance of SessionLabelStore.SessionLabel
        assertEquals("displayName", underTest.getDisplayName());

    }

    private static SessionLabel newSessionLabel(String displayName, String email) {
        try {
            Constructor<SessionLabel> ctor = SessionLabel.class.getDeclaredConstructor(String.class, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(displayName, email);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to instantiate SessionLabel for test", ex);
        }
    }
}
