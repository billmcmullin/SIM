package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DbEmailConfigProviderTest {

    private DbEmailConfigProvider provider;

    @BeforeEach
    void setUp() {
        provider = mock(DbEmailConfigProvider.class);

        // Enable real execution of interface default methods
        when(provider.loadOptional()).thenCallRealMethod();
        when(provider.mergeKeepingExistingPasswordIfBlank(any())).thenCallRealMethod();
    }

    @Test
    void loadOptional_returnsEmpty_whenLoadIsNull() {
        when(provider.load()).thenReturn(null);

        Optional<EmailConfig> result = provider.loadOptional();

        assertTrue(result.isEmpty());
        verify(provider).load();
    }

    @Test
    void loadOptional_returnsValue_whenLoadHasConfig() {
        EmailConfig cfg = new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "user",
                "secret",
                "noreply@example.com"
        );
        when(provider.load()).thenReturn(cfg);

        Optional<EmailConfig> result = provider.loadOptional();

        assertTrue(result.isPresent());
        assertSame(cfg, result.get());
        verify(provider).load();
    }

    @Test
    void mergeKeepingExistingPasswordIfBlank_returnsNull_whenIncomingIsNull() {
        EmailConfig result = provider.mergeKeepingExistingPasswordIfBlank(null);

        assertNull(result);
        verify(provider, never()).load();
    }

    @Test
    void mergeKeepingExistingPasswordIfBlank_returnsIncoming_whenIncomingPasswordIsNonBlank() {
        EmailConfig incoming = new EmailConfig(
                "smtp.example.com",
                587,
                true,
                true,
                false,
                "user",
                "newPassword",
                "noreply@example.com"
        );

        EmailConfig result = provider.mergeKeepingExistingPasswordIfBlank(incoming);

        assertSame(incoming, result);
        verify(provider, never()).load();
    }

    @Test
    void mergeKeepingExistingPasswordIfBlank_usesExistingPassword_whenIncomingPasswordIsBlank() {
        EmailConfig incoming = new EmailConfig(
                "smtp.new.com",
                2525,
                true,
                false,
                true,
                "newUser",
                "   ", // blank
                "alerts@example.com"
        );
        EmailConfig existing = new EmailConfig(
                "smtp.old.com",
                587,
                true,
                true,
                false,
                "oldUser",
                "existingSecret",
                "old@example.com"
        );
        when(provider.load()).thenReturn(existing);

        EmailConfig result = provider.mergeKeepingExistingPasswordIfBlank(incoming);

        assertNotNull(result);
        assertEquals("smtp.new.com", result.host());
        assertEquals(2525, result.port());
        assertTrue(result.auth());
        assertFalse(result.startTls());
        assertTrue(result.ssl());
        assertEquals("newUser", result.username());
        assertEquals("existingSecret", result.password()); // retained from DB
        assertEquals("alerts@example.com", result.defaultFrom());
        verify(provider).load();
    }

    @Test
    void mergeKeepingExistingPasswordIfBlank_setsEmptyPassword_whenIncomingBlankAndNoExisting() {
        EmailConfig incoming = new EmailConfig(
                "smtp.new.com",
                2525,
                true,
                false,
                true,
                "newUser",
                "", // blank
                "alerts@example.com"
        );
        when(provider.load()).thenReturn(null);

        EmailConfig result = provider.mergeKeepingExistingPasswordIfBlank(incoming);

        assertNotNull(result);
        assertEquals("", result.password());
        verify(provider).load();
    }

    @Test
    void mergeKeepingExistingPasswordIfBlank_normalizesNullUsernameAndDefaultFrom_toEmpty() {
        EmailConfig incoming = new EmailConfig(
                "smtp.new.com",
                2525,
                true,
                false,
                true,
                null,   // should become ""
                null,   // blank/null password => trigger merge path
                null    // should become ""
        );
        EmailConfig existing = new EmailConfig(
                "smtp.old.com",
                587,
                true,
                true,
                false,
                "oldUser",
                null,   // existing null password => ""
                "old@example.com"
        );
        when(provider.load()).thenReturn(existing);

        EmailConfig result = provider.mergeKeepingExistingPasswordIfBlank(incoming);

        assertNotNull(result);
        assertEquals("", result.username());
        assertEquals("", result.password());
        assertEquals("", result.defaultFrom());
        verify(provider).load();
    }
}
