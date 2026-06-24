package com.sim.chatserver.email;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface EmailService {

    void send(EmailMessage message);

    default CompletableFuture<Void> sendAsync(EmailMessage message) {
        Executor executor = Executors.newCachedThreadPool();
        return CompletableFuture.runAsync(() -> send(message), executor);
    }
}
