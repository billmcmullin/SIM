package com.sim.chatserver.email;

public record EmailAttachment(
        String fileName,
        String contentType,
        byte[] content
        ) {

    public EmailAttachment {
        if (fileName == null) {
            throw new NullPointerException("fileName is required");
        }
        if (contentType == null) {
            throw new NullPointerException("contentType is required");
        }
        if (content == null) {
            throw new NullPointerException("content is required");
        }
    }
}
