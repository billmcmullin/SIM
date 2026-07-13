package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmailConfigResolverTest {

    @Test
    void resolve_prefersEnv_whenEnvIsUsable() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);
        EmailConfig env = new EmailConfig("smtp.env.com", 587, true, true, false, "u", "p", "from@x.com");

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(env);

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertNotNull(result);
            assertEquals(EmailConfigSource.ENV, result.source());
            assertTrue(result.valid());
            assertSame(env, result.config());
            assertEquals("Using SMTP config from ENV", result.message());

            loader.verify(EmailConfigLoader::loadEnvOnly, times(1));
            loader.verify(EmailConfigLoader::loadPropertiesOnly, never());
            verifyNoInteractions(db);
        }
    }

    @Test
    void resolve_usesProperties_whenEnvNotUsable_andPropertiesUsable() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);
        EmailConfig badEnv = new EmailConfig("   ", 587, false, false, false, "", "", "");
        EmailConfig props = new EmailConfig("smtp.props.com", 2525, false, false, false, "", "", "");

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(badEnv);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(props);

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.PROPERTIES, result.source());
            assertTrue(result.valid());
            assertSame(props, result.config());
            assertEquals("Using SMTP config from properties", result.message());

            loader.verify(EmailConfigLoader::loadEnvOnly, times(1));
            loader.verify(EmailConfigLoader::loadPropertiesOnly, times(1));
            verifyNoInteractions(db);
        }
    }

    @Test
    void resolve_usesDatabase_whenEnvAndPropertiesNotUsable() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);
        EmailConfig dbCfg = new EmailConfig("smtp.db.com", 465, true, false, true, "dbu", "dbp", "db@x.com");

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(null);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(null);
            when(db.load()).thenReturn(dbCfg);

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.DATABASE, result.source());
            assertTrue(result.valid());
            assertSame(dbCfg, result.config());
            assertEquals("Using SMTP config from database", result.message());

            verify(db, times(1)).load();
        }
    }

    @Test
    void resolve_returnsNone_whenAllSourcesMissingOrInvalid() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(null);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(
                    new EmailConfig("smtp.props.com", 70000, false, false, false, "", "", "")
            );
            when(db.load()).thenReturn(null);

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.NONE, result.source());
            assertFalse(result.valid());
            assertNull(result.config());
            assertEquals("No valid email configuration found in ENV, properties, or database.", result.message());

            verify(db, times(1)).load();
        }
    }

    @Test
    void resolve_returnsNone_whenDbProviderNull_andOtherSourcesInvalid() {
        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(null);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(null);

            EmailConfigResolver resolver = new EmailConfigResolver(null);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.NONE, result.source());
            assertFalse(result.valid());
            assertNull(result.config());
            assertEquals("No valid email configuration found in ENV, properties, or database.", result.message());
        }
    }

    @Test
    void resolve_handlesDbException_andReturnsNone_whenNoOtherUsableConfig() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(null);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(null);
            when(db.load()).thenThrow(new RuntimeException("db down"));

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.NONE, result.source());
            assertFalse(result.valid());
            assertNull(result.config());
            verify(db, times(1)).load();
        }
    }

    @Test
    void resolve_treatsBoundaryPorts_asExpected() {
        DbEmailConfigProvider db = mock(DbEmailConfigProvider.class);

        EmailConfig envBad0 = new EmailConfig("smtp.env.com", 0, false, false, false, "", "", "");
        EmailConfig propsBad65536 = new EmailConfig("smtp.props.com", 65536, false, false, false, "", "", "");
        EmailConfig dbGood65535 = new EmailConfig("smtp.db.com", 65535, false, false, false, "", "", "");

        try (MockedStatic<EmailConfigLoader> loader = mockStatic(EmailConfigLoader.class)) {
            loader.when(EmailConfigLoader::loadEnvOnly).thenReturn(envBad0);
            loader.when(EmailConfigLoader::loadPropertiesOnly).thenReturn(propsBad65536);
            when(db.load()).thenReturn(dbGood65535);

            EmailConfigResolver resolver = new EmailConfigResolver(db);
            ResolvedEmailConfig result = resolver.resolve();

            assertEquals(EmailConfigSource.DATABASE, result.source());
            assertTrue(result.valid());
            assertEquals(65535, result.config().port());
        }
    }
}
