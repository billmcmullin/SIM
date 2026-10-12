package com.sim.chatserver.salesforce;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

import com.sim.chatserver.util.CommonLiteralConstants;

public final class SalesforceClientException extends Exception {

    private static final long serialVersionUID = 1L;

    private final transient int statusCode;

    SalesforceClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException(CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}
