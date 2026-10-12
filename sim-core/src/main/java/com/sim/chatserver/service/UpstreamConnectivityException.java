package com.sim.chatserver.service;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

import com.sim.chatserver.util.CommonLiteralConstants;

final class UpstreamConnectivityException extends IOException {

    private static final long serialVersionUID = 1L;
    private static final String EMPTY = "";

    private final transient String code;

    UpstreamConnectivityException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    String code() {
        return code == null ? EMPTY : code;
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException(CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}
