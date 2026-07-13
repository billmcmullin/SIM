package com.sim.chatserver.email;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class EmailConfigLoaderTest {

    @AfterEach
    void cleanupEnv() {
        EmailConfigLoader.resetEnvAccessorForTests();
    }

    @Test
    void load_fallsBackToLocalhost_whenNoEnvAndNoProps() {
        // clear relevant env keys
        Map<String, String> env = new HashMap<>(System.getenv());
        env.remove("MAIL_HOST");
        env.remove("MAIL_PORT");
        env.remove("MAIL_AUTH");
        env.remove("MAIL_STARTTLS");
        env.remove("MAIL_SSL");
        env.remove("MAIL_USERNAME");
        env.remove("MAIL_PASSWORD");
        env.remove("MAIL_FROM");
        env.remove("MAIL_CONFIG_FILE");
        useEnv(env);

        EmailConfig cfg = EmailConfigLoader.load();

        assertNotNull(cfg);
        assertEquals("localhost", cfg.host());
        assertEquals(25, cfg.port());
        assertFalse(cfg.auth());
        assertFalse(cfg.startTls());
        assertFalse(cfg.ssl());
        assertEquals("", cfg.username());
        assertEquals("", cfg.password());
        assertEquals("", cfg.defaultFrom());
    }

    @Test
    void loadEnvOnly_returnsNull_whenMissingRequiredHostOrPort() {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.remove("MAIL_HOST");
        env.put("MAIL_PORT", "587");
        useEnv(env);
        assertNull(EmailConfigLoader.loadEnvOnly());

        env.put("MAIL_HOST", "smtp.example.com");
        env.remove("MAIL_PORT");
        useEnv(env);
        assertNull(EmailConfigLoader.loadEnvOnly());
    }

    @Test
    void loadEnvOnly_returnsConfig_whenValidRequiredValues() {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("MAIL_HOST", " smtp.example.com ");
        env.put("MAIL_PORT", "587");
        env.put("MAIL_AUTH", "yes");
        env.put("MAIL_STARTTLS", "on");
        env.put("MAIL_SSL", "1");
        env.put("MAIL_USERNAME", " user ");
        env.put("MAIL_PASSWORD", " pass ");
        env.put("MAIL_FROM", " noreply@example.com ");
        useEnv(env);

        EmailConfig cfg = EmailConfigLoader.loadEnvOnly();

        assertNotNull(cfg);
        assertEquals("smtp.example.com", cfg.host());
        assertEquals(587, cfg.port());
        assertTrue(cfg.auth());
        assertTrue(cfg.startTls());
        assertTrue(cfg.ssl());
        assertEquals("user", cfg.username());
        assertEquals("pass", cfg.password());
        assertEquals("noreply@example.com", cfg.defaultFrom());
    }

    @Test
    void loadPropertiesOnly_readsExternalFile_whenMailConfigFileSet() throws IOException {
        Path temp = Files.createTempFile("smtp-", ".properties");
        Files.writeString(temp, """
                mail.host=smtp.file.example.com
                mail.port=2525
                mail.auth=true
                mail.starttls=false
                mail.ssl=yes
                mail.username=fileUser
                mail.password=filePass
                mail.from=file@example.com
                """);

        try {
            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("MAIL_CONFIG_FILE", temp.toString());
            env.remove("MAIL_HOST");
            env.remove("MAIL_PORT");
            useEnv(env);

            EmailConfig cfg = EmailConfigLoader.loadPropertiesOnly();

            assertNotNull(cfg);
            assertEquals("smtp.file.example.com", cfg.host());
            assertEquals(2525, cfg.port());
            assertTrue(cfg.auth());
            assertFalse(cfg.startTls());
            assertTrue(cfg.ssl());
            assertEquals("fileUser", cfg.username());
            assertEquals("filePass", cfg.password());
            assertEquals("file@example.com", cfg.defaultFrom());
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void loadPropertiesOnly_returnsNull_whenExternalFileInvalidOrMissingRequired() throws IOException {
        Path temp = Files.createTempFile("smtp-invalid-", ".properties");
        Files.writeString(temp, """
                mail.host=smtp.file.example.com
                mail.port=99999
                """);

        try {
            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("MAIL_CONFIG_FILE", temp.toString());
            useEnv(env);

            EmailConfig cfg = EmailConfigLoader.loadPropertiesOnly();
            assertNull(cfg);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void private_parsePort_handlesValidAndInvalidValues() throws Exception {
        Method m = EmailConfigLoader.class.getDeclaredMethod("parsePort", String.class, String.class);
        m.setAccessible(true);

        assertEquals(25, m.invoke(null, "25", "test"));
        assertEquals(65535, m.invoke(null, "65535", "test"));
        assertNull(m.invoke(null, "0", "test"));
        assertNull(m.invoke(null, "65536", "test"));
        assertNull(m.invoke(null, "abc", "test"));
        assertNull(m.invoke(null, null, "test"));
    }

    @Test
    void private_parseBoolean_handlesTruthyAndDefaultCases() throws Exception {
        Method m = EmailConfigLoader.class.getDeclaredMethod("parseBoolean", String.class, boolean.class);
        m.setAccessible(true);

        assertTrue((boolean) m.invoke(null, "true", false));
        assertTrue((boolean) m.invoke(null, "YES", false));
        assertTrue((boolean) m.invoke(null, "1", false));
        assertTrue((boolean) m.invoke(null, "on", false));
        assertTrue((boolean) m.invoke(null, "y", false));

        assertFalse((boolean) m.invoke(null, "false", true)); // only explicit truthy returns true
        assertFalse((boolean) m.invoke(null, "nope", false));
        assertTrue((boolean) m.invoke(null, null, true));    // default used
        assertFalse((boolean) m.invoke(null, "   ", false)); // trimToNull -> default
    }

    private static void useEnv(Map<String, String> env) {
        EmailConfigLoader.setEnvAccessorForTests(env::get);
    }
}
