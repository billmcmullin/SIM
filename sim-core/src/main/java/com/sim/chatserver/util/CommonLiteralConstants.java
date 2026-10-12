package com.sim.chatserver.util;

/**
 * Shared string literals used across modules to reduce duplication.
 */
public final class CommonLiteralConstants {

    // parasoft-begin-suppress CDD.DUPS "Centralized constant registry intentionally stores shared literals used across modules."

    private CommonLiteralConstants() {
    }

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_X_API_KEY = "X-API-Key";
    public static final String HEADER_AUTHORIZATION_LOWER = "authorization";
    public static final String HEADER_X_API_KEY_LOWER = "x-api-key";
    public static final String AUTH_BEARER_PREFIX = "Bearer ";
    public static final String HTTP_METHOD_POST = "POST";

    public static final String URL_PREFIX_HTTP = "http://";
    public static final String URL_PREFIX_HTTPS = "https://";
    public static final String REGEX_TRAILING_SLASHES = "/+$";

    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_SEPARATOR = "://";

    public static final String ESCAPED_SINGLE_QUOTE = "\\'";
    public static final String ELLIPSIS = "...";
    public static final String LOG_KV_METHOD_POST = "method=POST";
    public static final String LOG_KV_STATUS = "status=";
    public static final String LOG_KV_BODY = "body=";
    public static final String LOG_KV_URL = "url=";
    public static final String LOG_KV_MESSAGE = "message=";
    public static final String LOG_UNABLE_TO_FORWARD_LOGIN = "Unable to forward unauthenticated user to login";

    public static final String MSG_DESERIALIZATION_NOT_ALLOWED = "Deserialization is not allowed.";
    public static final String MSG_REQUEST_HANDLING_FAILED = "Request handling failed.";
    public static final String MSG_REQUEST_FAILED = "Request failed.";
    public static final String MSG_FALLBACK_SEND_ERROR_FAILED = "Fallback sendError failed";
    public static final String MSG_FALLBACK_SEND_ERROR_WITH_PERIOD = "Failed sending fallback server error.";
    public static final String MSG_INVALID_FORWARD_PATH = "Invalid forward path.";
    public static final String MSG_UNABLE_TO_LIST_WIDGETS = "Unable to list widgets";
    public static final String MSG_UNABLE_TO_WRITE_JSON_ERROR_RESPONSE = "Unable to write JSON error response";
    public static final String MSG_UNABLE_TO_WRITE_JSON_RESPONSE = "Unable to write JSON response";
    public static final String MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX = "Unable to inspect table metadata for ";
    public static final String MSG_UNABLE_TO_WRITE_RESPONSE_NO_PERIOD = "Unable to write response";
    public static final String MSG_TEMPLATE_NOT_FOUND_FORMAT = "Template not found: {0}";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String TEMPLATE_CONTEXT_PATH = "${contextPath}";
    public static final String TEMPLATE_USER = "${user}";
    public static final String CONTENT_TYPE_HTML_UTF8 = "text/html;charset=UTF-8";
    public static final String CONTENT_TYPE_HTML_UTF8_SPACED = "text/html; charset=UTF-8";
    public static final String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=UTF-8";
    public static final String MSG_LOAD_SERVER_CONFIGURATION = "Unable to load server configuration";
    public static final String LOG_UNHANDLED_EXCEPTION_IN_DO_GET = "Unhandled exception in doGet";

    public static final String COMPONENT_TEST_CONNECTION_SERVLET = "test-connection-servlet";
    public static final String TEXT_NO_VALID_API_KEY = "no valid api key";
    public static final String TEXT_UNAUTHORIZED = "unauthorized";
    public static final String MSG_UNABLE_TO_WRITE_RESPONSE = "Unable to write response.";

    public static final String FIELD_REQUEST_ID = "requestId";
    public static final String FIELD_MODE = "mode";
    public static final String FIELD_TOTAL_SELECTED = "totalSelected";
    public static final String FIELD_TOTAL_BATCHES = "totalBatches";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_LATENCY_MS = "latencyMs";
    public static final String FIELD_PROMPT = "prompt";
    public static final String FIELD_RESPONSE = "response";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_SESSION_ID = "sessionId";
    public static final String FIELD_ENTRY_COUNT = "entryCount";
    public static final String FIELD_META = "meta";
    public static final String FIELD_PROGRESS_PCT = "progressPct";
    public static final String FIELD_STATUS_TEXT = "statusText";
    public static final String FIELD_ALL_SELECTED_CHAT_IDS = "allSelectedChatIds";
    public static final String FIELD_USED_CHAT_IDS = "usedChatIds";
    public static final String FIELD_FAILED_BATCH_INDEXES = "failedBatchIndexes";
    public static final String FIELD_COVERAGE_COMPLETE = "coverageComplete";
    public static final String FIELD_MISSING_CHAT_IDS = "missingChatIds";
    public static final String FIELD_ATTACHMENTS = "attachments";
    public static final String FIELD_LABELS = "labels";
    public static final String FIELD_VALUES = "values";
    public static final String FIELD_DAYS = "days";
    public static final String FIELD_MATCH_PATTERN = "matchPattern";
    public static final String FIELD_MATCH_TYPE = "matchType";
    public static final String FIELD_TERM = "term";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CHAT_ID = "chatId";
    public static final String FIELD_QUERY = "query";
    public static final String FIELD_ACTIVITY = "activity";
    public static final String FIELD_FROM = "from";
    public static final String FIELD_TEXT_RESPONSE = "textResponse";
    public static final String FIELD_TOTAL = "total";
    public static final String FIELD_RANGE_START = "rangeStart";
    public static final String FIELD_RANGE_END = "rangeEnd";
    public static final String FIELD_WIDGET = "widget";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_DISPLAY_LABEL = "displayLabel";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_SESSIONS = "sessions";
    public static final String FIELD_LIMIT = "limit";
    public static final String FIELD_ROWS = "rows";
    public static final String FIELD_RANK = "rank";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_ERROR = "error";
    public static final String KEY_WIDGET_ID = "widgetId";
    public static final String KEY_TABLE_NAME = "tableName";
    public static final String KEY_TABLE_EXISTS = "tableExists";
    public static final String KEY_COUNT = "count";
    public static final String KEY_DAY = "day";
    public static final String KEY_SLOT = "slot";
    public static final String KEY_STATUS = "status";
    public static final String KEY_TEXT = "text";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_SYNCED = "synced";
    public static final String KEY_SELECTED_CHAT_IDS = "selectedChatIds";
    public static final String PATH_LOGIN = "/log" + "in";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_SELECTION_ID = "selectionId";
    public static final String PATH_WIDGET_REVIEW = "/dashboard/widgets/drilldown/review";

    public static final String FORMAT_CSV = "csv";
    public static final String FORMAT_JSON = "json";

    public static final String TOSTRING_REQUEST_ID_PREFIX = "requestId='";
    public static final String TOSTRING_TOTAL_BATCHES_PREFIX = ", totalBatches=";
    public static final String TOSTRING_COVERAGE_COMPLETE_PREFIX = ", coverageComplete=";
    public static final String ERR_REQUIRED_SUFFIX = " is required";
    public static final String DEFAULT_MODE_CHAT = "chat";

    public static final String MSG_AUTHENTICATION_REQUIRED = "Authentication required.";
    public static final String MSG_ADMIN_ROLE_REQUIRED = "Admin role required.";
    public static final String MSG_INVALID_JSON_PAYLOAD = "Invalid JSON payload.";
    public static final String LOG_UNHANDLED_EXCEPTION_IN_DO_POST = "Unhandled exception in doPost";
    public static final String STATUS_DOWN = "DOWN";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_IDLE = "idle";
    public static final String MSG_INVALID_SQL_IDENTIFIER = "Invalid SQL identifier";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String DB_TABLE_TYPE_TABLE = "TABLE";

    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String VALUE_YES = "yes";
    public static final String VALUE_ALL = "all";
    public static final String VALUE_LOW = "low";
    public static final String VALUE_DOWN_LOWER = "down";
    public static final String VALUE_RESET = "reset";
    public static final String VALUE_NA = "N/A";
    public static final String VALUE_USER = "user";
    public static final String VALUE_UNKNOWN_LOWER = "unknown";
    public static final String VALUE_UNKNOWN_UPPER = "UNKNOWN";
    public static final String STATUS_FAILED = "failed";
    public static final String DATETIME_PATTERN_SECONDS = "yyyy-MM-dd HH:mm:ss";
    public static final String MARKER_UNKNOWN = "(unknown)";
    public static final String MARKER_EMPTY_PARENS = "(empty)";
    public static final String NULL_CHAR_STRING = "\u0000";
    public static final String ROLE = "role";
    public static final String REGEX_WHITESPACE = "\\s+";
    public static final String REGEX_NON_ALNUM_UNDERSCORE = "[^A-Za-z0-9_]";
    public static final String REGEX_CONTROL_CHARS = "[\\u0000-\\u001F\\u007F]";
    public static final String REGEX_CTRL_EXCEPT_CRLF_TAB = "[\\p{Cntrl}&&[^\\r\\n\\t]]";
    public static final String SUPPRESS_UNUSED = "unused";
    public static final String HOST_LOCALHOST = "localhost";
    public static final String SQL_IDENTIFIER_REGEX = "^[A-Za-z_][A-Za-z0-9_]{0,62}$";
    public static final String DOUBLE_QUOTE_ESCAPED = "\"\"";
    public static final String MATCH_TYPE_WILDCARD = "WILDCARD";

    public static final String COMPONENT_WIDGET_SYNC = "widget-sync";
    public static final String LABEL_SUMMARY = "summary";
    public static final String LOGGER_OWASP = "OWASP";
    public static final String LOG_HANDLED_EXCEPTION = "Handled exception";

    public static final String SQL_WHERE_CREATED_AT_RANGE = " WHERE created_at >= ? AND created_at < ?";

    public static final String HTML_ESCAPE_QUOT = "&quot;";
    public static final String HTML_ESCAPE_APOS = "&#39;";
    public static final String ESCAPED_CARRIAGE_RETURN = "\\r";

    public static final String SQL_COL_CREATED_AT = "created_at";
    public static final String SQL_COL_UPDATED_AT = "updated_at";
    public static final String SQL_COL_SESSION_ID = "session_id";
    public static final String SQL_COL_WIDGET_CHAT_ID = "widget_chat_id";
    public static final String SQL_COL_LAST_ENTRY = "last_entry";
    public static final String SQL_COL_RESPONSE_TEXT = "response_text";
    public static final String SQL_SELECT_PREFIX = "SELECT ";
    public static final String SQL_SELECT_SESSION_COUNTS_PREFIX = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM ";
    public static final String SQL_SESSION_ID_NOT_NULL_NON_EMPTY_GROUP_BY = " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
    public static final String SQL_SELECT_WIDGET_CHAT_PREFIX = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM ";

    public static final String HTML_ESCAPE_AMP = "&amp;";
    public static final String HTML_ESCAPE_LT = "&lt;";
    public static final String HTML_ESCAPE_GT = "&gt;";

    public static final String JSON_ESCAPE_BACKSLASH_TO = "\\\\";
    public static final String JSON_ESCAPE_QUOTE_TO = "\\\"";
    public static final String JSON_ESCAPE_NEWLINE_TO = "\\n";

    // parasoft-end-suppress CDD.DUPS "Centralized constant registry intentionally stores shared literals used across modules."
}