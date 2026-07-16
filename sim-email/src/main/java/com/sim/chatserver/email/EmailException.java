package com.sim.chatserver.email;

public class EmailException extends RuntimeException {

    EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
