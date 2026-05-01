package com.sim.chatserver.term;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for TermChatSnapshot
 *
 * @see com.sim.chatserver.term.TermChatSnapshot
 * @author bmcmullin
 */
public class TermChatSnapshotTest
{

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        Object obj = null; // UTA: configured value
        boolean result = underTest.equals(obj);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for equals(Object)
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#equals(Object)
     * @author bmcmullin
     */
    @Test
    public void testEquals2() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        TermChatSnapshot obj = mock(TermChatSnapshot.class);
        boolean result = underTest.equals(obj);

        // Then - assertions for result of method equals(Object)
        assertFalse(result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatId()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getChatId()
     * @author bmcmullin
     */
    @Test
    public void testGetChatId() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getChatId();

        // Then - assertions for result of method getChatId()
        assertEquals("chatId", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        Timestamp result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertNotNull(result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPrompt()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getPrompt()
     * @author bmcmullin
     */
    @Test
    public void testGetPrompt() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getPrompt();

        // Then - assertions for result of method getPrompt()
        assertEquals("prompt", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getResponse()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getResponse()
     * @author bmcmullin
     */
    @Test
    public void testGetResponse() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getResponse();

        // Then - assertions for result of method getResponse()
        assertEquals("response", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertEquals("sessionId", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermName()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getTermName()
     * @author bmcmullin
     */
    @Test
    public void testGetTermName() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getTermName();

        // Then - assertions for result of method getTermName()
        assertEquals("termName", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getWidgetId()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#getWidgetId()
     * @author bmcmullin
     */
    @Test
    public void testGetWidgetId() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        String result = underTest.getWidgetId();

        // Then - assertions for result of method getWidgetId()
        assertEquals("widgetId", result);

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for hashCode()
     *
     * @see com.sim.chatserver.term.TermChatSnapshot#hashCode()
     * @author bmcmullin
     */
    @Test
    public void testHashCode() throws Throwable
    {
        // Given
        String termName = "termName"; // UTA: default value
        String widgetId = "widgetId"; // UTA: default value
        String chatId = "chatId"; // UTA: default value
        String prompt = "prompt"; // UTA: default value
        String response = "response"; // UTA: default value
        Timestamp createdAt = mock(Timestamp.class);
        String sessionId = "sessionId"; // UTA: default value
        TermChatSnapshot underTest = new TermChatSnapshot(termName, widgetId, chatId, prompt, response, createdAt, sessionId);

        // When
        int result = underTest.hashCode();

        // Then - assertions for result of method hashCode()
        // assertEquals(1, result);// UTA: Expected value may be unstable

        // Then - assertions for this instance of TermChatSnapshot
        assertAll(() -> {
            assertEquals("termName", underTest.getTermName());
        }, () -> {
            assertEquals("widgetId", underTest.getWidgetId());
        }, () -> {
            assertEquals("chatId", underTest.getChatId());
        }, () -> {
            assertEquals("prompt", underTest.getPrompt());
        }, () -> {
            assertEquals("response", underTest.getResponse());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertEquals("sessionId", underTest.getSessionId());
        });

    }
}
