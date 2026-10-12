package com.sim.chatserver.email;

public record EmailAttachment(
        String fileName,
        String contentType,
        byte[] content
        ) {

    private static final String MSG_FILE_NAME_REQUIRED = "fileName is required";
    private static final String MSG_CONTENT_TYPE_REQUIRED = "contentType is required";
    private static final String MSG_CONTENT_REQUIRED = "content is required";

    public EmailAttachment {
        if (fileName == null) {
            throw new NullPointerException(MSG_FILE_NAME_REQUIRED);
        }
        if (contentType == null) {
            throw new NullPointerException(MSG_CONTENT_TYPE_REQUIRED);
        }
        if (content == null) {
            throw new NullPointerException(MSG_CONTENT_REQUIRED);
        }
    }
}
