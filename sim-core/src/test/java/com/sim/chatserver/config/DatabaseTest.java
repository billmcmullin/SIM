package com.sim.chatserver.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DatabaseTest {

    @Test
    void load_succeeds_whenRequiredEnvIsPresent() throws Exception {
        ProbeResult result = runProbe("load", true, false);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains("PASS:load"), result.output);
    }

    @Test
    void load_fails_whenRequiredEnvIsMissing() throws Exception {
        ProbeResult result = runProbe("load", true, true);

        assertNotEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains("Environment variable DB_HOST is required."), result.output);
    }

    @Test
    void requireValidPort_rejectsNonNumericValues() throws Exception {
        ProbeResult result = runProbe("invalidPort", true, false);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains("PASS:invalidPort"), result.output);
    }

    @Test
    void requireValidHost_rejectsInvalidCharacters() throws Exception {
        ProbeResult result = runProbe("invalidHost", true, false);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains("PASS:invalidHost"), result.output);
    }

    @Test
    void requireValidDbName_rejectsInvalidCharacters() throws Exception {
        ProbeResult dbNameResult = runProbe("invalidDbName", true, false);

        assertEquals(0, dbNameResult.exitCode, dbNameResult.output);
        assertTrue(dbNameResult.output.contains("PASS:invalidDbName"), dbNameResult.output);
    }

    private static ProbeResult runProbe(String scenario,
            boolean setValidDbEnv,
            boolean removeHostAfterSetup) throws Exception {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        String classPath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(
                javaExecutable,
                "-cp",
                classPath,
                "com.sim.chatserver.config.DatabaseProbe",
                scenario
        );
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        env.remove("DB_HOST");
        env.remove("DB_PORT");
        env.remove("DB_NAME");
        env.remove("DB_USER");
        env.remove("DB_PASSWORD");

        if (setValidDbEnv) {
            env.put("DB_HOST", "localhost");
            env.put("DB_PORT", "5432");
            env.put("DB_NAME", "simdb");
            env.put("DB_USER", "sim_user");
            env.put("DB_PASSWORD", "sim_password");
        }
        if (removeHostAfterSetup) {
            env.remove("DB_HOST");
        }

        Process process = pb.start();
        String output;
        try (var stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        int exitCode = process.waitFor();
        return new ProbeResult(exitCode, output);
    }

    private record ProbeResult(int exitCode, String output) {
    }
}
