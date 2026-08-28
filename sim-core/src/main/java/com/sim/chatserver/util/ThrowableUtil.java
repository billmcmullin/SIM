package com.sim.chatserver.util;

public final class ThrowableUtil {

    private ThrowableUtil() {
    }

    public static boolean hasInterruptedCause(Throwable throwable) {
        if (throwable instanceof InterruptedException) {
            return true;
        }

        Throwable cause1 = nextCause(throwable);
        if (cause1 instanceof InterruptedException) {
            return true;
        }

        Throwable cause2 = nextCause(cause1);
        if (cause2 instanceof InterruptedException) {
            return true;
        }

        Throwable cause3 = nextCause(cause2);
        if (cause3 instanceof InterruptedException) {
            return true;
        }

        Throwable cause4 = nextCause(cause3);
        if (cause4 instanceof InterruptedException) {
            return true;
        }

        Throwable cause5 = nextCause(cause4);
        if (cause5 instanceof InterruptedException) {
            return true;
        }

        Throwable cause6 = nextCause(cause5);
        if (cause6 instanceof InterruptedException) {
            return true;
        }

        Throwable cause7 = nextCause(cause6);
        if (cause7 instanceof InterruptedException) {
            return true;
        }

        Throwable cause8 = nextCause(cause7);
        return cause8 instanceof InterruptedException;
    }

    private static Throwable nextCause(Throwable current) {
        if (current == null) {
            return null;
        }
        Throwable cause = current.getCause();
        if (cause == null || cause.equals(current)) {
            return null;
        }
        return cause;
    }
}