package com.sim.chatserver.widget;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import com.sim.chatserver.util.CommonLiteralConstants;

public final class DuplicateWidgetIdException extends SQLException {

    private static final long serialVersionUID = 1L;

    private static final String MSG_PREFIX = "Widget ID '";
    private static final String MSG_SUFFIX = "' already exists.";

    DuplicateWidgetIdException(String widgetId, Throwable cause) {
        super(MSG_PREFIX + widgetId + MSG_SUFFIX, cause);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException(CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}
