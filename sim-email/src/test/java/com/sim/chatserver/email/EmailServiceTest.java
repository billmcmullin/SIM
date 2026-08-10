package com.sim.chatserver.email;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class EmailServiceTest {

    @Test
    void sendAsync_invokesSendWithSameMessage() throws Exception {
        CapturingEmailService service = new CapturingEmailService();
        EmailMessage message = EmailMessage.builder()
                .to("to@example.com")
                .subject("subject")
                .textBody("body")
                .build();

        CompletableFuture<Void> future = service.sendAsync(message);
        future.get(3, TimeUnit.SECONDS);

        assertTrue(service.awaitSend());
        assertSame(message, service.lastMessage);
    }

    private static final class CapturingEmailService implements EmailService {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile EmailMessage lastMessage;

        @Override
        public void send(EmailMessage message) {
            this.lastMessage = message;
            latch.countDown();
        }

        boolean awaitSend() throws InterruptedException {
            return latch.await(3, TimeUnit.SECONDS);
        }
    }
}
