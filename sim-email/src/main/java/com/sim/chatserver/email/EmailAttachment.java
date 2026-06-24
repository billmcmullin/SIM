package com.sim.chatserver.email;

import java.util.Objects;

public record EmailAttachment(
        String fileName,
        String contentType,
        byte[] content
        ) {

    public EmailAttachment {
        Objects.requireNonNull(fileName, "fileName is required");
        Objects.requireNonNull(contentType, "contentType is required");
        Objects.requireNonNull(content, "content is required");
    }
}
