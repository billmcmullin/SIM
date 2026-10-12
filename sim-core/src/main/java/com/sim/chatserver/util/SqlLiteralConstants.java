package com.sim.chatserver.util;

/**
 * Shared SQL/string literals to reduce repeated inline string usage.
 */
public final class SqlLiteralConstants {

    private SqlLiteralConstants() {
    }

    public static final String COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
    public static final String COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
    public static final String COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
    public static final String COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
    public static final String COL_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_SESSION_ID;
    public static final String COL_FIRST_SEEN = "first_seen";

    public static final String SQL_SELECT_COUNT_FROM = "SELECT COUNT(*) FROM ";
    public static final String SQL_SELECT_PROMPT_CREATED_AT_FROM = "SELECT prompt, created_at FROM ";
    public static final String SQL_SELECT_WIDGET_CHAT_ID_FROM = "SELECT widget_chat_id FROM ";
    public static final String SQL_SELECT_WIDGET_CHAT_PROMPT_RESPONSE_CREATED_SESSION_FROM =
            com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;

    public static final String SQL_WHERE = " WHERE ";
    public static final String SQL_AND = " AND ";
    public static final String SQL_ILIKE_OR = " ILIKE ? OR ";

    public static final String SQL_WHERE_CREATED_AT_RANGE = com.sim.chatserver.util.CommonLiteralConstants.SQL_WHERE_CREATED_AT_RANGE;
    public static final String SQL_ORDER_BY_CREATED_AT_DESC = " ORDER BY created_at DESC";
    public static final String SQL_SESSION_ID_NOT_NULL_GROUP_BY =
            " WHERE session_id IS NOT NULL GROUP BY session_id";
    public static final String SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY =
            com.sim.chatserver.util.CommonLiteralConstants.SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY;

    public static final String SQL_INVALID_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;

    public static final String DOUBLE_QUOTE = "\"";
    public static final String DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
    public static final String COMMA_SPACE = ", ";
    public static final String QUESTION_MARK = "?";
    public static final String DB_TYPE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.KEY_TEXT;
}
