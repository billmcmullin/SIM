package com.sim.chatserver.email;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

final class EmailMessageTestBuilder {

    private static final Method BUILDER_FACTORY = lookupMethod(EmailMessage.class, "builder");

    private final Object delegate;

    private EmailMessageTestBuilder(Object delegate) {
        this.delegate = delegate;
    }

    static EmailMessageTestBuilder builder() {
        try {
            BUILDER_FACTORY.setAccessible(true);
            Object delegate = BUILDER_FACTORY.invoke(null);
            return new EmailMessageTestBuilder(delegate);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to construct EmailMessage.Builder for test", ex);
        }
    }

    EmailMessageTestBuilder from(String from) {
        invoke("from", String.class, from);
        return this;
    }

    EmailMessageTestBuilder to(String recipient) {
        invoke("to", String.class, recipient);
        return this;
    }

    EmailMessageTestBuilder to(List<String> recipients) {
        invoke("to", List.class, recipients);
        return this;
    }

    EmailMessageTestBuilder cc(String recipient) {
        invoke("cc", String.class, recipient);
        return this;
    }

    EmailMessageTestBuilder cc(List<String> recipients) {
        invoke("cc", List.class, recipients);
        return this;
    }

    EmailMessageTestBuilder bcc(String recipient) {
        invoke("bcc", String.class, recipient);
        return this;
    }

    EmailMessageTestBuilder bcc(List<String> recipients) {
        invoke("bcc", List.class, recipients);
        return this;
    }

    EmailMessageTestBuilder subject(String subject) {
        invoke("subject", String.class, subject);
        return this;
    }

    EmailMessageTestBuilder textBody(String textBody) {
        invoke("textBody", String.class, textBody);
        return this;
    }

    EmailMessageTestBuilder htmlBody(String htmlBody) {
        invoke("htmlBody", String.class, htmlBody);
        return this;
    }

    EmailMessageTestBuilder markdownBody(String markdownBody) {
        invoke("markdownBody", String.class, markdownBody);
        return this;
    }

    EmailMessageTestBuilder attachment(EmailAttachment attachment) {
        invoke("attachment", EmailAttachment.class, attachment);
        return this;
    }

    EmailMessageTestBuilder attachments(List<EmailAttachment> attachments) {
        invoke("attachments", List.class, attachments);
        return this;
    }

    EmailMessage build() {
        Object built = invoke("build");
        return (EmailMessage) built;
    }

    private Object invoke(String methodName, Class<?> parameterType, Object arg) {
        Method method = lookupMethod(delegate.getClass(), methodName, parameterType);
        try {
            method.setAccessible(true);
            return method.invoke(delegate, arg);
        } catch (InvocationTargetException ex) {
            rethrowCause(ex);
            return null;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to invoke EmailMessage.Builder method: " + methodName, ex);
        }
    }

    private Object invoke(String methodName) {
        Method method = lookupMethod(delegate.getClass(), methodName);
        try {
            method.setAccessible(true);
            return method.invoke(delegate);
        } catch (InvocationTargetException ex) {
            rethrowCause(ex);
            return null;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to invoke EmailMessage.Builder method: " + methodName, ex);
        }
    }

    private static Method lookupMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Missing expected method " + name + " on " + type.getName(), ex);
        }
    }

    private static void rethrowCause(InvocationTargetException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new AssertionError("Unexpected checked exception from EmailMessage.Builder", cause);
    }
}
