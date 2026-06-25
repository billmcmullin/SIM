package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class EmailConfigLoaderTest {

    private final Map<String, String> originalEnv = new HashMap<>(System.getenv());

    @AfterEach
    void cleanupEnv() {
        // best-effort restore
        setEnv(new HashMap<>(originalEnv));
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
        setEnv(env);

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
        setEnv(env);
        assertNull(EmailConfigLoader.loadEnvOnly());

        env.put("MAIL_HOST", "smtp.example.com");
        env.remove("MAIL_PORT");
        setEnv(env);
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
        setEnv(env);

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
            setEnv(env);

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
            setEnv(env);

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

    /**
     * Best-effort environment mutator for tests. On Java 16+ this may require:
     * --add-opens java.base/java.util=ALL-UNNAMED --add-opens
     * java.base/java.lang=ALL-UNNAMED
     */
    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newEnv) {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");

            Field theEnvironmentField = pe.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.clear();
            env.putAll(newEnv);

            Field theCaseInsensitiveEnvironmentField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.clear();
            cienv.putAll(newEnv);
            return;
        } catch (Exception ignored) {
            // fallback below
        }

        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field m = cl.getDeclaredField("m");
            m.setAccessible(true);
            Object obj = m.get(env);
            Map<String, String> map = (Map<String, String>) obj;
            map.clear();
            map.putAll(newEnv);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to set environment variables for test. "
                    + "If running on Java 21, add --add-opens for java.base/java.util and java.base/java.lang.", e);
        }
    }
}
