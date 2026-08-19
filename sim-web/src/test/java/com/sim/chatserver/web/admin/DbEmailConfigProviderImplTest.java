package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.email.EmailConfig;

class DbEmailConfigProviderImplTest {

    @Test
    void save_rejectsNullConfig() {
        DbEmailConfigProviderImpl provider = new DbEmailConfigProviderImpl();
        assertThrows(IllegalArgumentException.class, () -> provider.save(null, "tester"));
    }

    @Test
    void save_rejectsBlankHostBeforeDbAccess() {
        DbEmailConfigProviderImpl provider = new DbEmailConfigProviderImpl();
        EmailConfig invalid = new EmailConfig("   ", 587, true, true, false, "u", "p", "from@test.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> provider.save(invalid, "tester"));
        assertEquals("SMTP host is required", ex.getMessage());
    }

    @Test
    void save_rejectsOutOfRangePortBeforeDbAccess() {
        DbEmailConfigProviderImpl provider = new DbEmailConfigProviderImpl();

        EmailConfig low = new EmailConfig("smtp.test", 0, true, true, false, "u", "p", "from@test.com");
        EmailConfig high = new EmailConfig("smtp.test", 70000, true, true, false, "u", "p", "from@test.com");

        assertThrows(IllegalArgumentException.class, () -> provider.save(low, "tester"));
        assertThrows(IllegalArgumentException.class, () -> provider.save(high, "tester"));
    }

    @Test
    void internalStringHelpers_normalizeInputs() throws Exception {
        DbEmailConfigProviderImpl provider = new DbEmailConfigProviderImpl();

        assertEquals("abc", invoke(provider, "stripControls", new Class[]{String.class}, "a\u0000b\r\nc"));
        assertEquals("abc", invoke(provider, "trimToEmpty", new Class[]{String.class}, "  abc  "));
        assertEquals("", invoke(provider, "nullToEmpty", new Class[]{String.class}, (Object) null));
    }

    private static Object invoke(Object target, String methodName, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
