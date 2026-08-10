package com.sim.chatserver.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Small helper main used by DatabaseTest to exercise Database static initialization
 * and private validators in a process-isolated JVM.
 */
public final class DatabaseProbe {

    private DatabaseProbe() {
    }

    public static void main(String[] args) {
        String scenario = args.length == 0 ? "" : args[0];
        try {
            switch (scenario) {
                case "load":
                    runLoadScenario();
                    break;
                case "invalidPort":
                    runInvalidPortScenario();
                    break;
                case "invalidHost":
                    runInvalidHostScenario();
                    break;
                case "invalidDbName":
                    runInvalidDbNameScenario();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown scenario: " + scenario);
            }
            System.out.println("PASS:" + scenario);
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(2);
        }
    }

    private static void runLoadScenario() throws Exception {
        Class.forName("com.sim.chatserver.config.Database");
    }

    private static void runInvalidPortScenario() throws Exception {
        Class<?> databaseClass = Class.forName("com.sim.chatserver.config.Database");
        env(databaseClass).put("TEST_PORT", "not-a-number");
        expectIllegalState(databaseClass, "requireValidPort", "TEST_PORT", "must be a valid integer");
    }

    private static void runInvalidHostScenario() throws Exception {
        Class<?> databaseClass = Class.forName("com.sim.chatserver.config.Database");
        env(databaseClass).put("TEST_HOST", "bad host!");
        expectIllegalState(databaseClass, "requireValidHost", "TEST_HOST", "invalid host characters");
    }

    private static void runInvalidDbNameScenario() throws Exception {
        Class<?> databaseClass = Class.forName("com.sim.chatserver.config.Database");
        env(databaseClass).put("TEST_DB", "bad-name");
        expectIllegalState(databaseClass, "requireValidDbName", "TEST_DB", "letters, digits, or underscore");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> env(Class<?> databaseClass) throws Exception {
        Field field = databaseClass.getDeclaredField("ENV");
        field.setAccessible(true);
        return (Map<String, String>) field.get(null);
    }

    private static void expectIllegalState(Class<?> databaseClass,
            String methodName,
            String variableName,
            String expectedMessagePart) throws Exception {
        Method method = databaseClass.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);

        try {
            method.invoke(null, variableName);
            throw new AssertionError("Expected IllegalStateException for " + methodName + "(" + variableName + ")");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof IllegalStateException)) {
                throw new AssertionError("Expected IllegalStateException but got " + cause, cause);
            }
            String message = cause.getMessage();
            if (message == null || !message.contains(expectedMessagePart)) {
                throw new AssertionError("Unexpected message: " + message);
            }
        }
    }
}
