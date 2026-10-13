package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.security.email.EmailSecretCrypto;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.TextIoSanitizerUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin-only read/search explorer for external forum databases.
 *
 * Supported entities: users, conversations, messages.
 * Supported engines: PostgreSQL, MySQL.
 *
 * This endpoint only runs SELECT queries and marks the JDBC connection read-only.
 */
final class ForumDatabaseExplorerService {

    private static final Logger LOG = Logger.getLogger(ForumDatabaseExplorerService.class.getCanonicalName());

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]{1,253}$");
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,128}$");
    private static final Pattern USER_PATTERN = Pattern.compile("^[^\\u0000\\r\\n]{1,128}$");

    private static final int MAX_PASSWORD_LEN = 1024;
    private static final int MAX_SEARCH_LEN = 256;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int DEFAULT_REPLY_LIMIT = 200;
    private static final int MAX_REPLY_LIMIT = 500;
    private static final int MAX_OFFSET = 50_000;
    private static final int MAX_CELL_TEXT_LEN = 4_000;
    private static final int MAX_SHARE_TO_LEN = 4_000;
    private static final int MAX_SHARE_SUBJECT_LEN = 512;
    private static final int CONFIG_ROW_ID = 1;
    private static final String CONFIG_TABLE = "forum_db_explorer_config";
    private static final String SCHEMA_PUBLIC = "public";
    private static final String METADATA_TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
    private static final String DEFAULT_DB_TYPE = "postgresql";
    private static final String DEFAULT_UPDATED_BY = com.sim.chatserver.util.CommonLiteralConstants.VALUE_UNKNOWN_LOWER;
    private static final String DEFAULT_SHARE_SUBJECT = "Cleaned Forum Human Message";
    private static final String VALUE_REPLY = "reply";
    private static final String VALUE_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String ALIAS_DISCUSSION = "d";
    private static final String ALIAS_COMMENT = "cm";
    private static final String ALIAS_USER = "u";
    private static final String KEY_REPLY_COUNT = "replyCount";
    private static final String KEY_REPLIES = "replies";
    private static final String ACTION_QUERY = com.sim.chatserver.util.CommonLiteralConstants.FIELD_QUERY;
    private static final String ENTITY_USERS = "users";
    private static final String ENTITY_CONVERSATIONS = "conversations";
        private static final String ENTITY_MESSAGES = "messages";
        private static final String ENTITY_SESSIONS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSIONS;
        private static final String TOKEN_TEXT = com.sim.chatserver.util.CommonLiteralConstants.KEY_TEXT;
        private static final String TOKEN_CONVERSATION_ID = "conversation_id";
        private static final String TOKEN_CONVERSATIONID = "conversationid";
        private static final String TOKEN_CONVERSATION = "conversation";
        private static final String TOKEN_CREATED = "created";
        private static final String TOKEN_DATE = com.sim.chatserver.util.CommonLiteralConstants.PARAM_DATE;
        private static final String TOKEN_DISCUSSION_ID = "discussion_id";
        private static final String TOKEN_DISCUSSIONID = "discussionid";
        private static final String TOKEN_USER_ID = "user_id";
        private static final String TOKEN_JSON = com.sim.chatserver.util.CommonLiteralConstants.FORMAT_JSON;
        private static final String SQL_NULL_LITERAL = "NULL";
        private static final String SQL_LOWER_COALESCE_PREFIX = "LOWER(COALESCE(";
        private static final String SQL_OR_LOWER_COALESCE_PREFIX = " OR " + SQL_LOWER_COALESCE_PREFIX;
        private static final String SQL_LIKE_PARAM_SUFFIX = ",'')) LIKE ?";
        private static final String SQL_SELECT_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_PREFIX;
        private static final String SQL_FROM = " FROM ";
        private static final String SQL_WHERE_OPEN = " WHERE (";
        private static final String SQL_OR = " OR ";
        private static final String SQL_AS_DELIMITER = " AS ";
        private static final String SQL_EQUALS_DELIMITER = " = ";
        private static final String SQL_LEFT_JOIN = " LEFT JOIN ";
        private static final String SQL_JOIN = "JOIN ";
        private static final String SQL_WHERE = "WHERE ";
        private static final String SQL_ON = " ON ";
        private static final String SQL_ORDER_BY = " ORDER BY ";
        private static final String SQL_FROM_OPEN = " FROM (";
        private static final String SQL_COALESCE_NULLIF_TRIM_PREFIX = "COALESCE(NULLIF(TRIM(";
        private static final String SQL_EMPTY_FALLBACK_INFIX = "),''), ";
        private static final String SQL_WHEN_LOWER_COALESCE_PREFIX = "WHEN LOWER(COALESCE(";
        private static final String SQL_ANSWERED_ACCEPTED_THEN = ",'')) IN ('answered','accepted') THEN 1 ";
        private static final String TABLE_NAME_COLUMN = "TABLE_NAME";
        private static final String COLUMN_NAME_COLUMN = "COLUMN_NAME";
        private static final String COMMA_SPACE = ", ";
        private static final String SPACE = " ";
        private static final String KEY_DB_TYPE = "dbType";
        private static final String KEY_SEARCH = CommonLiteralConstants.PARAM_SEARCH;
        private static final String KEY_LIMIT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT;
        private static final String KEY_OFFSET = "offset";
        private static final String KEY_ROWS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_ROWS;
        private static final String KEY_READABLE_ONLY = "readableOnly";
        private static final String KEY_SHARE_TO = "shareTo";
        private static final String KEY_SHARE_SUBJECT = "shareSubject";
        private static final String KEY_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_UPDATED_AT;
        private static final String KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
        private static final String KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
        private static final String KEY_TABLE = "table";
        private static final String KEY_RETURNED = "returned";
        private static final String KEY_TOTAL = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL;
        private static final String KEY_COLUMNS = "columns";
        private static final String KEY_MESSAGE_TEXT_COLUMN = "messageTextColumn";
        private static final String KEY_RESPONSE_TEXT_COLUMN = "responseTextColumn";
        private static final String KEY_ACTOR_COLUMN = "actorColumn";
        private static final String KEY_ROLE_COLUMN = "roleColumn";
        private static final String KEY_CONVERSATION_COLUMN = "conversationColumn";
        private static final String KEY_CREATED_AT_COLUMN = "createdAtColumn";
        private static final String KEY_AVAILABLE_ENTITIES = "availableEntities";
        private static final String KEY_RESOLVED_TABLES = "resolvedTables";
        private static final String KEY_PREFERRED_ENTITY = "preferredEntity";
        private static final String KEY_ROLE = com.sim.chatserver.util.CommonLiteralConstants.ROLE;
        private static final String ROLE_ADMIN = com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN;
        private static final String VALUE_OK = "ok";
        private static final String KEY_ACTOR = "actor";
        private static final String KEY_MESSAGE_TEXT = "message_text";
        private static final String KEY_MESSAGE_ID = "message_id";
        private static final String KEY_STATUS_ID = "status_id";
        private static final String KEY_STATUS_NAME = "status_name";
        private static final String KEY_QNA_STATUS = "qna_status";
        private static final String KEY_TAG_NAMES = "tag_names";
        private static final String TOKEN_MESSAGE = KEY_MESSAGE;
        private static final String TOKEN_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
        private static final String TOKEN_ID = "id";
        private static final String TOKEN_HOST = "host";
        private static final String TOKEN_PORT = "port";
        private static final String TOKEN_DATABASE = "database";
        private static final String TOKEN_USERNAME = com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME;
        private static final String TOKEN_SUBJECT = "subject";
        private static final String TOKEN_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
        private static final String TOKEN_CHAIN_KEY = "chain_key";
        private static final String TOKEN_COMMUNITY_CONFIRMED = "community_confirmed";
        private static final String TOKEN_NAME = com.sim.chatserver.util.CommonLiteralConstants.FIELD_NAME;
        private static final ColumnHints EMPTY_COLUMN_HINTS = new ColumnHints(null, null, null, null, null, null);
        private static final String TOKEN_BODY = "body";
        private static final String TOKEN_CONTENT = "content";
        private static final String TOKEN_USERID = "userid";
        private static final String TOKEN_FULL_NAME = "full_name";
        private static final String TOKEN_DISPLAY_NAME = "display_name";
        private static final String TOKEN_CREATED_BY = "created_by";
        private static final String TOKEN_CREATEDON = "createdon";
        private static final String TOKEN_TIMESTAMP = "timestamp";
        private static final String TOKEN_INSERTUSERID = "insertuserid";
        private static final String TOKEN_INSERT_USER_ID = "insert_user_id";
        private static final String TOKEN_AUTHOR_ID = "author_id";
        private static final String TOKEN_SENDER_ID = "sender_id";
        private static final String TOKEN_DATEINSERTED = "dateinserted";
        private static final String TOKEN_DATE_INSERTED = "date_inserted";
        private static final String TOKEN_INSERTED_AT = "inserted_at";
        private static final String TOKEN_TIME = "time";
        private static final String TOKEN_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
        private static final String TOKEN_QUESTION = "question";
        private static final String TOKEN_USER_MESSAGE = "user_message";
        private static final String TOKEN_USER_TEXT = "user_text";
        private static final String TOKEN_POST_TEXT = "post_text";
        private static final String TOKEN_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE;
        private static final String TOKEN_ASSISTANT_RESPONSE = "assistant_response";
        private static final String TOKEN_ANSWER = "answer";
        private static final String TOKEN_BOT_RESPONSE = "bot_response";
        private static final String TOKEN_AUTHOR = "author";
        private static final String TOKEN_SENDER = "sender";
        private static final String TOKEN_SENDER_NAME = "sender_name";
        private static final String TOKEN_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
        private static final String TOKEN_THREAD_ID = "thread_id";
        private static final String TOKEN_CHAT_ID = "chat_id";
        private static final String TOKEN_DIALOG_ID = "dialog_id";
        private static final String TOKEN_TOPIC_ID = "topic_id";
        private static final String TOKEN_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
        private static final String TOKEN_SENT_AT = "sent_at";
        private static final String TOKEN_MESSAGE_TIME = "message_time";
        private static final String TOKEN_EVENT_TIME = "event_time";
        private static final String TOKEN_THREAD = "thread";
        private static final String TOKEN_CHAT = com.sim.chatserver.util.CommonLiteralConstants.DEFAULT_MODE_CHAT;
        private static final String TOKEN_SESSION = "session";
        private static final String TOKEN_TITLE = "title";
        private static final String TOKEN_QNA = "qna";
        private static final String TOKEN_RECORD_ID = "record_id";
        private static final String TOKEN_FULLNAME = "fullname";
        private static final String TOKEN_LABEL = "label";
        private static final String TOKEN_USER_NAME = "user_name";
        private static final String TOKEN_AUTHOR_NAME = "author_name";
        private static final String TOKEN_CREATED_BY_USER = "created_by_user";
        private static final String TOKEN_OWNER_ID = "owner_id";
        private static final String TOKEN_SENDER_TYPE = "sender_type";
        private static final String TOKEN_SESSIONID = "sessionid";
        private static final String TOKEN_AI_CONVERSATION_ID = "ai_conversation_id";
        private static final String TOKEN_AICONVERSATIONID = "aiconversationid";
        private static final String TOKEN_THREADID = "threadid";
        private static final String TOKEN_CHATID = "chatid";
        private static final String TOKEN_DIALOGID = "dialogid";
        private static final String TOKEN_DIALOG = "dialog";
        private static final String TOKEN_DISCUSSION = "discussion";
        private static final String TOKEN_TOPICID = "topicid";
        private static final String TOKEN_TOPIC = "topic";
        private static final String TOKEN_WIDGETCHATID = "widgetchatid";
        private static final String TOKEN_CONVERSATION_KEY = "conversation_key";
        private static final String TOKEN_SESSION_KEY = "session_key";
        private static final String TOKEN_CREATED_ON = "created_on";
        private static final String TOKEN_CREATEDAT = "createdat";
        private static final String TOKEN_POSTED_AT = "posted_at";
        private static final String TOKEN_INSERTED = "inserted";
        private static final String TOKEN_CHAR = "char";
        private static final String TOKEN_UUID = "uuid";
        private static final String TOKEN_STATUSID = "statusid";
        private static final String TOKEN_RECORD_STATUS_ID = "record_status_id";
        private static final String TOKEN_RECORDSTATUSID = "recordstatusid";
        private static final String TOKEN_TAG_ID = "tag_id";
        private static final String TOKEN_TAGID = "tagid";
        private static final String KEY_PASSWORD_STORED = "passwordStored";
        private static final String KEY_UPDATED_BY = "updatedBy";
        private static final String WILDCARD_ALL = "%";
        private static final String KEY_ENTITY = "entity";
        private static final String PARAM_ACTION = "action";
        private static final String PARAM_PASSWORD = com.sim.chatserver.util.CommonLiteralConstants.KEY_PASSWORD;
        private static final String PARAM_DISCUSSION_ID = "discussionId";
        private static final String PARAM_CONVERSATION_ID = "conversationId";
        private static final String ACTION_LOAD_CONFIG = "loadConfig";
        private static final String ACTION_SAVE_CONFIG = "saveConfig";
        private static final String ACTION_CONNECT = "connect";
        private static final String ACTION_THREAD_REPLIES = "threadReplies";
        private static final String TABLE_GDN_DISCUSSION = "GDN_Discussion";
        private static final String TABLE_GDN_COMMENT = "GDN_Comment";
        private static final String TABLE_GDN_USER = "GDN_User";
        private static final String TABLE_GDN_TAG_DISCUSSION = "GDN_TagDiscussion";
        private static final String TABLE_GDN_TAG = "GDN_Tag";
        private static final String TABLE_GDN_RECORD_STATUS = "GDN_RecordStatus";
        private static final String TABLE_RECORD_STATUS = "recordStatus";
        private static final String TABLE_GDN_STATUS = "GDN_Status";
        private static final String ALIAS_RS = "rs";
        private static final String ALIAS_TD = "td";
        private static final String ALIAS_TG = "tg";
        private static final String TOKEN_COMMENT = "comment";
        private static final String TOKEN_COMMENTID = "commentid";
        private static final String TOKEN_COMMENT_ID = "comment_id";
        private static final String TOKEN_QUESTION_STATUS = "question_status";
        private static final String TOKEN_RECORDID = "recordid";
        private static final String TOKEN_TAG = "tag";
        private static final String TOKEN_AUTHOR_TYPE = "author_type";
        private static final String TOKEN_MESSAGE_TYPE = "message_type";
        private static final String TOKEN_ORIGIN = "origin";
        private static final String TOKEN_SOURCE = "source";
        private static final String TOKEN_DIRECTION = "direction";
        private static final String TOKEN_TYPE_NAME = "TYPE_NAME";
        private static final String TOKEN_DATA_TYPE = "DATA_TYPE";
        private static final String TOKEN_ORDINAL_POSITION = "ORDINAL_POSITION";
        private static final String TOKEN_XML = "xml";
        private static final String TOKEN_ARCHIVE = "archive";
        private static final String TOKEN_BACKUP = "backup";
        private static final String TOKEN_AUDIT = "audit";
        private static final String TOKEN_HISTORY = "history";
        private static final String VALUE_PLUS = "+";
        private static final String VALUE_NUL_CHAR = com.sim.chatserver.util.CommonLiteralConstants.NULL_CHAR_STRING;
        private static final String VALUE_BACKTICK = "`";
        private static final String VALUE_DOUBLE_BACKTICK = "``";
        private static final String VALUE_DOUBLE_QUOTE = "\"";
        private static final String VALUE_DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
        private static final String SQL_PARAM_WITH_TRAILING_SPACE = "? ";
        private static final String SQL_ASC = " ASC ";
        private static final String SQL_LIMIT_QMARK = "LIMIT ?";
        private static final String SQL_LIMIT_OFFSET = " LIMIT ? OFFSET ?";
        private static final String SQL_DESC = " DESC";
        private static final String SQL_DESC_LIMIT_OFFSET = " DESC LIMIT ? OFFSET ?";
        private static final String SQL_SELECT_COUNT = "SELECT COUNT(*)";
        private static final String SQL_SELECT_COUNT_FROM = "SELECT COUNT(*) FROM ";
        private static final String SQL_CASE = "CASE ";
        private static final String SQL_LIKE_ANSWERED = ",'')) LIKE '%answered%' THEN 1 ";
        private static final String SQL_LIKE_ACCEPTED = ",'')) LIKE '%accepted%' THEN 1 ";
        private static final String SQL_ELSE_ZERO_END_AS = "ELSE 0 END AS ";
        private static final String SQL_GROUP_CONCAT_DISTINCT = "GROUP_CONCAT(DISTINCT ";
        private static final String SQL_GROUP_CONCAT_SEPARATOR = " SEPARATOR ', ')";
        private static final String SQL_STRING_AGG_DISTINCT = "STRING_AGG(DISTINCT ";
        private static final String SQL_STRING_AGG_SEPARATOR = ", ', ')";
        private static final String SQL_SELECT_OPEN = "(SELECT ";
        private static final String SQL_SELECT_ONE_FROM = "SELECT 1 FROM ";
        private static final String SQL_LIMIT_ONE = " LIMIT 1";
        private static final String SQL_FORUM_ROWS_ALIAS = ") forum_rows";
        private static final String SQL_CAST_AS_CHAR = " AS CHAR)";
        private static final String SQL_CAST_AS_TEXT = " AS TEXT)";
        private static final String ERR_REQUIRED_SUFFIX_WITH_PERIOD = " is required.";
        private static final String ERR_MUST_BE_BETWEEN = " must be between ";
        private static final String ERR_AND = " and ";
        private static final String ERR_MUST_BE_VALID_NUMBER = " must be a valid number.";
        private static final String MSG_INVALID_DB_TYPE = "dbType must be 'postgresql' or 'mysql'.";
        private static final String MSG_UNABLE_TO_LOAD_CONFIG = "Unable to load forum explorer configuration.";
        private static final String MSG_UNABLE_TO_READ_FROM_DATABASE_PREFIX = "Unable to read from database: ";
        private static final String LOG_FORUM_QUERY_FAILED = "Forum DB explorer query failed";
        private static final String LOG_UNABLE_TO_LOAD_CONFIG = "Unable to load forum explorer configuration";
        private static final String LOG_UNABLE_TO_SAVE_CONFIG = "Unable to save forum explorer configuration";
        private static final String LOG_FALLBACK_SEND_ERROR_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
        private static final String LOG_FORUM_TABLE_SWITCH = "Forum DB explorer switched messages table from {0} to {1} because primary table had no rows.";
        private static final String MSG_ADMIN_ROLE_REQUIRED = com.sim.chatserver.util.CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED;
        private static final String MSG_VALID_HOST_REQUIRED = "A valid host is required.";
        private static final String MSG_VALID_DATABASE_NAME_REQUIRED = "A valid database name is required (letters, digits, underscore, hyphen).";
        private static final String MSG_VALID_USERNAME_REQUIRED = "A valid username is required.";
        private static final String MSG_DATABASE_PASSWORD_REQUIRED = "A database password is required.";
        private static final String MSG_UNSUPPORTED_ACTION = "Unsupported action.";
        private static final String MSG_CONFIGURATION_LOADED = "Configuration loaded.";
        private static final String MSG_HOST_INVALID = "Host is invalid.";
        private static final String MSG_DATABASE_NAME_INVALID = "Database name is invalid (letters, digits, underscore, hyphen).";
        private static final String MSG_USERNAME_INVALID = "Username is invalid.";
        private static final String MSG_CONFIGURATION_SAVED = "Configuration saved.";
        private static final String MSG_UNABLE_TO_CONNECT_EXTERNAL_FORUM_DATABASE = "Unable to connect to external forum database.";
        private static final String MSG_UNABLE_TO_INITIALIZE_CONFIG_TABLE = "Unable to initialize forum explorer configuration table.";
        private static final String MSG_UNABLE_TO_LOAD_SAVED_CONFIG = "Unable to load saved forum explorer configuration.";
        private static final String MSG_UNABLE_TO_UPDATE_SAVED_CONFIG = "Unable to update saved forum explorer configuration.";
        private static final String MSG_UNABLE_TO_BIND_CONFIG_STATEMENT = "Unable to bind forum explorer configuration statement.";
        private static final String MSG_UNABLE_TO_OPEN_APP_DB_CONNECTION = "Unable to open application database connection.";
        private static final String MSG_UNABLE_TO_READ_NUMERIC_COLUMN_PREFIX = "Unable to read numeric column: ";
        private static final String MSG_CONNECTION_SUCCESSFUL = "Connection successful.";
        private static final String MSG_ENTITY_MUST_BE_ONE_OF = "entity must be one of: users, conversations, messages.";
        private static final String MSG_TABLE_NOT_FOUND_FOR_ENTITY_PREFIX = "Table not found in selected database for entity: ";
        private static final String MSG_SELECTED_TABLE_HAS_NO_READABLE_COLUMNS = "Selected table has no readable columns.";
        private static final String MSG_DISCUSSION_ID_REQUIRED = "discussionId is required.";
        private static final String MSG_UNABLE_TO_QUERY_TABLE_PREFIX = "Unable to query table: ";
        private static final String MSG_UNABLE_TO_QUERY_FORUM_THREAD_REPLIES = "Unable to query forum thread replies.";
        private static final String MSG_UNABLE_TO_RUN_DISCUSSION_THREAD_PROJECTION_QUERY = "Unable to run discussion thread projection query.";
        private static final String MSG_UNABLE_TO_BIND_SEARCH_PARAMS = "Unable to bind search parameters.";
        private static final String MSG_UNABLE_TO_LOCATE_TABLE_PREFIX = "Unable to locate table: ";
        private static final String MSG_UNABLE_TO_FIND_ALTERNATE_MESSAGES_TABLE = "Unable to find alternate messages table.";
        private static final String MSG_SKIPPING_CANDIDATE_TABLE_PREFIX = "Skipping candidate table without readable rows: ";
        private static final String MSG_UNABLE_TO_BIND_TABLE_SEARCH_PARAMS = "Unable to bind table search parameters.";
        private static final String MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX = "Unable to inspect table metadata for: ";
        private static final String MSG_UNABLE_TO_RESOLVE_TABLE_FOR_ENTITY_PREFIX = "Unable to resolve table for entity: ";
        private static final String MSG_UNABLE_TO_RESOLVE_TABLE_IN_SCHEMA = "Unable to resolve table in schema.";
        private static final String MSG_UNABLE_TO_FIND_RELAXED_MESSAGES_TABLE = "Unable to find relaxed messages table candidate.";
        private static final String MSG_UNABLE_TO_INSPECT_COLUMNS_FOR_TABLE_PREFIX = "Unable to inspect columns for table: ";
        private static final String MSG_INVALID_IDENTIFIER = "Invalid identifier.";
        private static final String MSG_UNKNOWN_ERROR = "Unknown error";
        private static final String MSG_UNABLE_TO_WRITE_EXPLORER_ERROR = "Unable to write explorer error payload";
        private static final String MSG_UNABLE_TO_WRITE_EXPLORER_JSON = "Unable to write explorer JSON payload";
        private static final String VALUE_TRUE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
        private static final String VALUE_FALSE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_FALSE;
        private static final String VALUE_ONE = "1";
        private static final String VALUE_ZERO = "0";
        private static final String VALUE_YES = com.sim.chatserver.util.CommonLiteralConstants.VALUE_YES;
        private static final String VALUE_NO = "no";
        private static final String VALUE_ON = "on";
        private static final String VALUE_OFF = "off";
        private static final String DB_TYPE_POSTGRES = "postgres";
        private static final String DB_TYPE_MYSQL = "mysql";
        private static final String SQL_CAST_PREFIX = "CAST(";
        private static final String COLUMN_DB_TYPE = "db_type";
        private static final String COLUMN_DATABASE_NAME = "database_name";
        private static final String COLUMN_PASSWORD_ENC = "password_enc";
        private static final String COLUMN_READABLE_ONLY = "readable_only";
        private static final String COLUMN_SHARE_TO = "share_to";
        private static final String COLUMN_SHARE_SUBJECT = "share_subject";
        private static final String COLUMN_UPDATED_BY = "updated_by";
        private static final String COLUMN_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_UPDATED_AT;
        private static final String SQL_CREATE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE + " ("
            + "id INTEGER PRIMARY KEY, "
            + "db_type VARCHAR(16), "
            + "host VARCHAR(253), "
            + "port INTEGER, "
            + "database_name VARCHAR(128), "
            + "username VARCHAR(128), "
            + "password_enc TEXT, "
            + "readable_only BOOLEAN NOT NULL DEFAULT TRUE, "
            + "share_to TEXT, "
            + "share_subject VARCHAR(512), "
            + "updated_by VARCHAR(128), "
            + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        private static final String SQL_SELECT_SAVED_CONFIG = "SELECT db_type, host, port, database_name, username, password_enc, readable_only, share_to, share_subject, "
            + "updated_by, updated_at FROM " + CONFIG_TABLE + " WHERE id = ?";
        private static final String SQL_UPDATE_SAVED_CONFIG = "UPDATE " + CONFIG_TABLE
            + " SET db_type = ?, host = ?, port = ?, database_name = ?, username = ?, password_enc = ?, readable_only = ?, "
            + "share_to = ?, share_subject = ?, updated_by = ?, updated_at = ? WHERE id = ?";
        private static final String SQL_INSERT_SAVED_CONFIG = "INSERT INTO " + CONFIG_TABLE
            + " (db_type, host, port, database_name, username, password_enc, readable_only, share_to, share_subject, updated_by, updated_at, id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        private static final String MSG_UNABLE_TO_SAVE_CONFIG = "Unable to save forum explorer configuration.";

        private static final List<String> ENTITY_ORDER = List.of(ENTITY_MESSAGES, ENTITY_CONVERSATIONS, ENTITY_USERS);
    private static final Set<String> ALLOWED_ENTITIES = Set.copyOf(ENTITY_ORDER);

    private static final Map<String, List<String>> ENTITY_TABLE_ALIASES = Map.of(
            ENTITY_USERS, List.of(ENTITY_USERS, VALUE_USER, "user_account", "user_accounts"),
            ENTITY_CONVERSATIONS, List.of(ENTITY_CONVERSATIONS, TOKEN_CONVERSATION, "chat_threads", "threads", "chats", ENTITY_SESSIONS),
            ENTITY_MESSAGES, List.of(ENTITY_MESSAGES, TOKEN_MESSAGE, "chat_messages", "posts", "forum_messages", "chat_history", "widget_chat"));

        private static final List<String> RELAXED_MESSAGE_NAME_TOKENS = List.of(
            TOKEN_MESSAGE, ENTITY_MESSAGES, TOKEN_CHAT, "forum", "post", TOKEN_THREAD, TOKEN_CONVERSATION, com.sim.chatserver.util.CommonLiteralConstants.FIELD_WIDGET);
        private static final List<String> RELAXED_MESSAGE_TEXT_COLUMNS = List.of(
            TOKEN_PROMPT, TOKEN_MESSAGE, TOKEN_BODY, TOKEN_CONTENT, TOKEN_TEXT, TOKEN_QUESTION, TOKEN_USER_MESSAGE, TOKEN_USER_TEXT, TOKEN_POST_TEXT, "input", ACTION_QUERY);
        private static final List<String> RELAXED_RESPONSE_COLUMNS = List.of(
                TOKEN_RESPONSE_TEXT, TOKEN_RESPONSE, TOKEN_ASSISTANT_RESPONSE, VALUE_REPLY, TOKEN_ANSWER, TOKEN_BOT_RESPONSE);
        private static final List<String> RELAXED_ACTOR_COLUMNS = List.of(
            TOKEN_USERNAME, VALUE_USER, TOKEN_AUTHOR, TOKEN_SENDER, TOKEN_SENDER_NAME, TOKEN_CREATED_BY, TOKEN_FULL_NAME, TOKEN_NAME);
        private static final List<String> RELAXED_CONVERSATION_COLUMNS = List.of(
            TOKEN_SESSION_ID, TOKEN_CONVERSATION_ID, TOKEN_THREAD_ID, TOKEN_CHAT_ID, TOKEN_DIALOG_ID, TOKEN_TOPIC_ID, TOKEN_WIDGET_CHAT_ID);
        private static final List<String> RELAXED_CREATED_AT_COLUMNS = List.of(
            TOKEN_CREATED_AT, TOKEN_CREATED, TOKEN_CREATEDON, TOKEN_TIMESTAMP, TOKEN_SENT_AT, TOKEN_MESSAGE_TIME, TOKEN_TIME, TOKEN_DATE, TOKEN_EVENT_TIME);

    private static final String SESSION_USER = VALUE_USER;
    private static final String SESSION_ROLE = KEY_ROLE;
    private static final String EMPTY = "";

    void handlePost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, MSG_ADMIN_ROLE_REQUIRED);
            return;
        }

        String action = normalizeParam(ServletRequestParamUtil.firstParam(req, PARAM_ACTION, 16, true, true));
        if (action == null) {
            action = ACTION_QUERY;
        }

        if (ACTION_LOAD_CONFIG.equalsIgnoreCase(action)) {
            handleLoadConfig(resp);
            return;
        }
        if (ACTION_SAVE_CONFIG.equalsIgnoreCase(action)) {
            handleSaveConfig(req, resp);
            return;
        }

        DbType dbType = DbType.from(ServletRequestParamUtil.firstParam(req, KEY_DB_TYPE, 32, true, true));
        if (dbType == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_INVALID_DB_TYPE);
            return;
        }

        String host = readHost(req);
        String database = readDatabase(req);
        String username = readUsername(req);
        String password = readPassword(req);
        String portRaw = readPortRaw(req);

        if (!isValidHost(host)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_VALID_HOST_REQUIRED);
            return;
        }
        if (!isValidDatabaseName(database)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    MSG_VALID_DATABASE_NAME_REQUIRED);
            return;
        }
        if (!isValidUsername(username)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_VALID_USERNAME_REQUIRED);
            return;
        }
        if (password == null || password.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_DATABASE_PASSWORD_REQUIRED);
            return;
        }

        int port;
        try {
            port = parseIntWithBounds(portRaw, 1, 65535, TOKEN_PORT);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, "Invalid port parameter in forum explorer request", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }

        try (Connection connection = openExternalConnection(dbType, host, port, database, username, password)) {
            connection.setReadOnly(true);

            if (ACTION_CONNECT.equalsIgnoreCase(action)) {
                writeConnectResponse(resp, dbType, connection);
                return;
            }

            if (ACTION_THREAD_REPLIES.equalsIgnoreCase(action)) {
                handleThreadReplies(req, resp, connection, dbType);
                return;
            }

            if (!ACTION_QUERY.equalsIgnoreCase(action)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_UNSUPPORTED_ACTION);
                return;
            }

            String entity = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_ENTITY, 64, true, true));
            String search = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_SEARCH, MAX_SEARCH_LEN, true, true));

            int limit = DEFAULT_LIMIT;
            String limitRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_LIMIT, 8, true, true));
            if (limitRaw != null) {
                limit = parseIntWithBounds(limitRaw, 1, MAX_LIMIT, KEY_LIMIT);
            }

            int offset = 0;
            String offsetRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_OFFSET, 16, true, true));
            if (offsetRaw != null) {
                offset = parseIntWithBounds(offsetRaw, 0, MAX_OFFSET, KEY_OFFSET);
            }

            QueryResult result = runReadOnlyQuery(connection, dbType, entity, search, limit, offset);

            JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                    .add(KEY_STATUS, VALUE_OK)
                    .add(KEY_DB_TYPE, dbType.id)
                    .add(KEY_ENTITY, result.entity)
                        .add(KEY_TABLE, result.tableName)
                    .add(KEY_LIMIT, result.limit)
                    .add(KEY_OFFSET, result.offset)
                        .add(KEY_RETURNED, result.returned)
                        .add(KEY_TOTAL, result.total)
                        .add(KEY_COLUMNS, result.columns)
                    .add(KEY_ROWS, result.rows);
                    addOptionalString(payloadBuilder, KEY_MESSAGE_TEXT_COLUMN, result.messageTextColumn);
                    addOptionalString(payloadBuilder, KEY_RESPONSE_TEXT_COLUMN, result.responseTextColumn);
                    addOptionalString(payloadBuilder, KEY_ACTOR_COLUMN, result.actorColumn);
                    addOptionalString(payloadBuilder, KEY_ROLE_COLUMN, result.roleColumn);
                    addOptionalString(payloadBuilder, KEY_CONVERSATION_COLUMN, result.conversationColumn);
                    addOptionalString(payloadBuilder, KEY_CREATED_AT_COLUMN, result.createdAtColumn);
            JsonObject payload = payloadBuilder.build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.FINE, "Invalid forum explorer request parameters", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, safeErrorMessage(ex));
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, LOG_FORUM_QUERY_FAILED, ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    MSG_UNABLE_TO_READ_FROM_DATABASE_PREFIX + safeErrorMessage(ex));
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, LOG_FORUM_QUERY_FAILED, ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    MSG_UNABLE_TO_READ_FROM_DATABASE_PREFIX + safeErrorMessage(ex));
        }
    }

    private void handleLoadConfig(HttpServletResponse resp) {
        try (Connection conn = openAppConnection()) {
            ensureConfigTable(conn);
            SavedConfig config = loadSavedConfig(conn);

            JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add(KEY_STATUS, VALUE_OK)
                    .add(KEY_MESSAGE, MSG_CONFIGURATION_LOADED);
            addSavedConfigJson(payload, config);
            writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, LOG_UNABLE_TO_LOAD_CONFIG, ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_LOAD_CONFIG);
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, LOG_UNABLE_TO_LOAD_CONFIG, ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_LOAD_CONFIG);
        }
    }

    private void handleSaveConfig(HttpServletRequest req, HttpServletResponse resp) {
        String dbTypeRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_DB_TYPE, 32, true, true));
        DbType dbType = DbType.from(dbTypeRaw);
        if (dbTypeRaw != null && dbType == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_INVALID_DB_TYPE);
            return;
        }

        String host = readHost(req);
        String database = readDatabase(req);
        String username = readUsername(req);
        String password = readPassword(req);
        String portRaw = readPortRaw(req);
        String readableOnlyRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_READABLE_ONLY, 8, true, true));
        String shareTo = normalizeMultilineParam(ServletRequestParamUtil.firstParam(req, KEY_SHARE_TO, MAX_SHARE_TO_LEN, true, true));
        String shareSubject = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_SHARE_SUBJECT, MAX_SHARE_SUBJECT_LEN, true, true));

        if (host != null && !isValidHost(host)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_HOST_INVALID);
            return;
        }
        if (database != null && !isValidDatabaseName(database)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    MSG_DATABASE_NAME_INVALID);
            return;
        }
        if (username != null && !isValidUsername(username)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_USERNAME_INVALID);
            return;
        }

        Integer port = null;
        if (portRaw != null) {
            try {
                port = Integer.valueOf(parseIntWithBounds(portRaw, 1, 65535, TOKEN_PORT));
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, "Invalid forum explorer config port", ex);
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                return;
            }
        }

        boolean readableOnly = parseBooleanParam(readableOnlyRaw, true);
        String updatedBy = resolveUpdatedBy(req);

        try (Connection conn = openAppConnection()) {
            ensureConfigTable(conn);
            SavedConfig existing = loadSavedConfig(conn);

            String savedPasswordEnc = existing == null ? null : existing.passwordEnc;
            if (password != null && !password.isBlank()) {
                savedPasswordEnc = encryptSecret(password);
            }

            SavedConfig toSave = new SavedConfig(
                    dbType == null ? null : dbType.id,
                    host,
                    port,
                    database,
                    username,
                    savedPasswordEnc,
                    readableOnly,
                    shareTo,
                    shareSubject,
                    updatedBy,
                    Timestamp.from(Instant.now())
            );

            upsertSavedConfig(conn, toSave);
            SavedConfig saved = loadSavedConfig(conn);

            JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add(KEY_STATUS, VALUE_OK)
                    .add(KEY_MESSAGE, MSG_CONFIGURATION_SAVED);
            addSavedConfigJson(payload, saved);
            writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (ForumExplorerDataAccessException ex) {
            LOG.log(Level.WARNING, LOG_UNABLE_TO_SAVE_CONFIG, ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_SAVE_CONFIG);
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, LOG_UNABLE_TO_SAVE_CONFIG, ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_TO_SAVE_CONFIG);
        }
    }

    private String readHost(HttpServletRequest req) {
        return normalizeParam(ServletRequestParamUtil.firstParam(req, TOKEN_HOST, 300, true, true));
    }

    private String readDatabase(HttpServletRequest req) {
        return normalizeParam(ServletRequestParamUtil.firstParam(req, TOKEN_DATABASE, 128, true, true));
    }

    private String readUsername(HttpServletRequest req) {
        return normalizeParam(ServletRequestParamUtil.firstParam(req, TOKEN_USERNAME, 128, true, true));
    }

    private String readPassword(HttpServletRequest req) {
        return normalizeParam(ServletRequestParamUtil.firstParam(req, PARAM_PASSWORD, MAX_PASSWORD_LEN, true, true));
    }

    private String readPortRaw(HttpServletRequest req) {
        return normalizeParam(ServletRequestParamUtil.firstParam(req, TOKEN_PORT, 8, true, true));
    }

    private Connection openExternalConnection(DbType dbType,
            String host,
            int port,
            String database,
            String username,
            String password) {
        try {
            DataSource dataSource = dbType.externalDataSource(host, port, database, username, password);
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_CONNECT_EXTERNAL_FORUM_DATABASE, ex);
        }
    }

    private void ensureConfigTable(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SQL_CREATE_CONFIG_TABLE)) {
            ps.execute();
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_INITIALIZE_CONFIG_TABLE, ex);
        }
    }

    private SavedConfig loadSavedConfig(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_SAVED_CONFIG)) {
            ps.setInt(1, CONFIG_ROW_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String dbType = normalizeParam(rs.getString(COLUMN_DB_TYPE));
                String host = normalizeParam(rs.getString(TOKEN_HOST));
                Integer port = readNullableInt(rs, TOKEN_PORT);
                String database = normalizeParam(rs.getString(COLUMN_DATABASE_NAME));
                String username = normalizeParam(rs.getString(TOKEN_USERNAME));
                String passwordEnc = normalizeParam(rs.getString(COLUMN_PASSWORD_ENC));
                boolean readableOnly = rs.getBoolean(COLUMN_READABLE_ONLY);
                String shareTo = normalizeMultilineParam(rs.getString(COLUMN_SHARE_TO));
                String shareSubject = normalizeParam(rs.getString(COLUMN_SHARE_SUBJECT));
                String updatedBy = normalizeParam(rs.getString(COLUMN_UPDATED_BY));
                Timestamp updatedAt = rs.getTimestamp(COLUMN_UPDATED_AT);

                return new SavedConfig(
                        dbType,
                        host,
                        port,
                        database,
                        username,
                        passwordEnc,
                        readableOnly,
                        shareTo,
                        shareSubject,
                        updatedBy,
                        updatedAt
                );
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_LOAD_SAVED_CONFIG, ex);
        }
    }

    private void upsertSavedConfig(Connection conn, SavedConfig config) {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SAVED_CONFIG)) {
            bindSavedConfig(ps, config);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                return;
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_UPDATE_SAVED_CONFIG, ex);
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_SAVED_CONFIG)) {
            bindSavedConfig(ps, config);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_SAVE_CONFIG, ex);
        }
    }

    private void bindSavedConfig(PreparedStatement ps, SavedConfig config) {
        try {
            ps.setString(1, config.dbType);
            ps.setString(2, config.host);
            if (config.port == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setObject(3, config.port, Types.INTEGER);
            }
            ps.setString(4, config.database);
            ps.setString(5, config.username);
            ps.setString(6, config.passwordEnc);
            ps.setBoolean(7, config.readableOnly);
            ps.setString(8, config.shareTo);
            ps.setString(9, config.shareSubject);
            ps.setString(10, config.updatedBy);
            ps.setTimestamp(11, config.updatedAt);
            ps.setInt(12, CONFIG_ROW_ID);
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_BIND_CONFIG_STATEMENT, ex);
        }
    }

    private void addSavedConfigJson(JsonObjectBuilder payload, SavedConfig config) {
        if (payload == null) {
            return;
        }

        if (config == null) {
                payload.add(KEY_DB_TYPE, DEFAULT_DB_TYPE)
                .add(TOKEN_HOST, EMPTY)
                .add(TOKEN_PORT, EMPTY)
                .add(TOKEN_DATABASE, EMPTY)
                .add(TOKEN_USERNAME, EMPTY)
                    .add(KEY_PASSWORD_STORED, false)
                    .add(KEY_READABLE_ONLY, true)
                    .add(KEY_SHARE_TO, EMPTY)
                    .add(KEY_SHARE_SUBJECT, DEFAULT_SHARE_SUBJECT)
                    .add(KEY_UPDATED_BY, EMPTY)
                    .add(KEY_UPDATED_AT, EMPTY);
            return;
        }

        payload.add(KEY_DB_TYPE, config.dbType == null ? DEFAULT_DB_TYPE : config.dbType)
            .add(TOKEN_HOST, config.host == null ? EMPTY : config.host)
            .add(TOKEN_PORT, config.port == null ? EMPTY : config.port.toString())
            .add(TOKEN_DATABASE, config.database == null ? EMPTY : config.database)
            .add(TOKEN_USERNAME, config.username == null ? EMPTY : config.username)
                .add(KEY_PASSWORD_STORED, config.passwordEnc != null && !config.passwordEnc.isBlank())
                .add(KEY_READABLE_ONLY, config.readableOnly)
                .add(KEY_SHARE_TO, config.shareTo == null ? EMPTY : config.shareTo)
                .add(KEY_SHARE_SUBJECT, config.shareSubject == null ? DEFAULT_SHARE_SUBJECT : config.shareSubject)
                .add(KEY_UPDATED_BY, config.updatedBy == null ? EMPTY : config.updatedBy)
                .add(KEY_UPDATED_AT, config.updatedAt == null ? EMPTY : config.updatedAt.toInstant().toString());
    }

    private Connection openAppConnection() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_OPEN_APP_DB_CONNECTION, ex);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private static Integer readNullableInt(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            if (rs.wasNull()) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_READ_NUMERIC_COLUMN_PREFIX + column + '.', ex);
        }
    }

    private static boolean parseBooleanParam(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        if (VALUE_TRUE.equals(lower) || VALUE_ONE.equals(lower) || VALUE_YES.equals(lower) || VALUE_ON.equals(lower)) {
            return true;
        }
        if (VALUE_FALSE.equals(lower) || VALUE_ZERO.equals(lower) || VALUE_NO.equals(lower) || VALUE_OFF.equals(lower)) {
            return false;
        }
        return fallback;
    }

    private static String resolveUpdatedBy(HttpServletRequest req) {
        if (req == null) {
            return DEFAULT_UPDATED_BY;
        }
        HttpSession session = req.getSession(false);
        if (session == null) {
            return DEFAULT_UPDATED_BY;
        }
        Object user = session.getAttribute(SESSION_USER);
        if (user == null) {
            return DEFAULT_UPDATED_BY;
        }
        String normalized = normalizeParam(String.valueOf(user));
        return normalized == null ? DEFAULT_UPDATED_BY : normalized;
    }

    private static String encryptSecret(String plainText) {
        return EmailSecretCrypto.encrypt(plainText == null ? EMPTY : plainText);
    }

    private void writeConnectResponse(HttpServletResponse resp, DbType dbType, Connection connection) {
        JsonArrayBuilder available = Json.createArrayBuilder();
        JsonObjectBuilder resolvedTables = Json.createObjectBuilder();
        String preferredEntity = null;
        for (String entity : ENTITY_ORDER) {
            String resolvedTable = findTableName(connection, entity);
            if (resolvedTable != null) {
                available.add(entity);
                resolvedTables.add(entity, resolvedTable);
                if (preferredEntity == null) {
                    preferredEntity = entity;
                }
            }
        }

        JsonObjectBuilder payload = Json.createObjectBuilder()
            .add(KEY_STATUS, VALUE_OK)
                .add(KEY_DB_TYPE, dbType.id)
            .add(KEY_MESSAGE, MSG_CONNECTION_SUCCESSFUL)
                .add(KEY_AVAILABLE_ENTITIES, available)
                .add(KEY_RESOLVED_TABLES, resolvedTables);

        if (preferredEntity != null) {
            payload.add(KEY_PREFERRED_ENTITY, preferredEntity);
        }

        writeJson(resp, HttpServletResponse.SC_OK, payload.build());
    }

    private QueryResult runReadOnlyQuery(Connection connection,
            DbType dbType,
            String entity,
            String search,
            int limit,
            int offset) {
        String normalizedEntity = entity == null ? EMPTY : entity.toLowerCase(Locale.ROOT);
        if (!ALLOWED_ENTITIES.contains(normalizedEntity)) {
            throw new IllegalArgumentException(MSG_ENTITY_MUST_BE_ONE_OF);
        }

        if (ENTITY_MESSAGES.equals(normalizedEntity)) {
            QueryResult gdnThreadResult = tryRunGdnForumThreadQuery(connection, dbType, search, limit, offset);
            if (gdnThreadResult != null) {
                return gdnThreadResult;
            }
        }

        String tableName = findTableName(connection, normalizedEntity);
        if (tableName == null) {
            throw new IllegalArgumentException(MSG_TABLE_NOT_FOUND_FOR_ENTITY_PREFIX + normalizedEntity);
        }

        QueryResult primary = runReadOnlyQueryForTable(connection, dbType, normalizedEntity, tableName, search, limit, offset);

        if (ENTITY_MESSAGES.equals(normalizedEntity) && primary.total <= 0) {
            String alternate = findAlternativeMessageTableWithRows(connection, dbType, tableName);
            if (alternate != null && !alternate.equalsIgnoreCase(tableName)) {
                QueryResult alternateResult = runReadOnlyQueryForTable(connection, dbType, normalizedEntity, alternate, search, limit, offset);
                if (alternateResult.total > 0) {
                    LOG.log(Level.INFO, LOG_FORUM_TABLE_SWITCH,
                            new Object[]{tableName, alternate});
                    return alternateResult;
                }
            }
        }

        return primary;
    }

    private QueryResult runReadOnlyQueryForTable(Connection connection,
            DbType dbType,
            String normalizedEntity,
            String tableName,
            String search,
            int limit,
            int offset) {

        TableInfo tableInfo = loadTableInfo(connection, dbType, tableName);
        if (tableInfo.columns.isEmpty()) {
            throw new IllegalArgumentException(MSG_SELECTED_TABLE_HAS_NO_READABLE_COLUMNS);
        }

        String trimmedSearch = search == null ? EMPTY : search.trim();
        boolean hasSearch = !trimmedSearch.isEmpty() && !tableInfo.searchableColumns.isEmpty();
        ColumnHints hints = detectColumnHints(tableInfo.columns);

        String quotedTable = quoteIdentifier(dbType, tableName);
        String whereClause = buildWhereClause(dbType, tableInfo.searchableColumns, hasSearch);

        String orderColumn = tableInfo.columns.contains(TOKEN_ID) ? TOKEN_ID : tableInfo.columns.get(0);

        String dataSql = SQL_SELECT_PREFIX
                + joinQuotedColumns(dbType, tableInfo.columns)
            + SQL_FROM + quotedTable
                + whereClause
            + SQL_ORDER_BY + quoteIdentifier(dbType, orderColumn) + SQL_DESC_LIMIT_OFFSET;

        try {
            JsonArrayBuilder rows = Json.createArrayBuilder();
            int returned = 0;
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                int nextParam = bindSearchParams(statement, tableInfo.searchableColumns, trimmedSearch, hasSearch, dbType);
                statement.setInt(nextParam++, limit);
                statement.setInt(nextParam, offset);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder row = Json.createObjectBuilder();
                        for (String column : tableInfo.columns) {
                            addJsonCell(row, column, rs.getObject(column));
                        }
                        rows.add(row);
                        returned++;
                    }
                }
            }

            String countSql = SQL_SELECT_COUNT_FROM + quotedTable + whereClause;
            int total = 0;
            try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
                bindSearchParams(countStmt, tableInfo.searchableColumns, trimmedSearch, hasSearch, dbType);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

            JsonArrayBuilder columns = Json.createArrayBuilder();
            for (String column : tableInfo.columns) {
                columns.add(column);
            }

            return new QueryResult(
                    normalizedEntity,
                    tableName,
                    limit,
                    offset,
                    returned,
                    total,
                    columns,
                    rows,
                    hints.messageTextColumn,
                    hints.responseTextColumn,
                    hints.actorColumn,
                    hints.roleColumn,
                    hints.conversationColumn,
                    hints.createdAtColumn
            );
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_QUERY_TABLE_PREFIX + tableName + '.', ex);
        }
    }

    private void handleThreadReplies(HttpServletRequest req,
            HttpServletResponse resp,
            Connection connection,
            DbType dbType) {
        String discussionId = normalizeParam(ServletRequestParamUtil.firstParam(req, PARAM_DISCUSSION_ID, 128, true, true));
        if (discussionId == null) {
            discussionId = normalizeParam(ServletRequestParamUtil.firstParam(req, PARAM_CONVERSATION_ID, 128, true, true));
        }
        if (discussionId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_DISCUSSION_ID_REQUIRED);
            return;
        }

        int limit = DEFAULT_REPLY_LIMIT;
        String limitRaw = normalizeParam(ServletRequestParamUtil.firstParam(req, KEY_LIMIT, 8, true, true));
        if (limitRaw != null) {
            limit = parseIntWithBounds(limitRaw, 1, MAX_REPLY_LIMIT, KEY_LIMIT);
        }

        try {
            String discussionTable = findNamedTableIgnoreCase(connection, TABLE_GDN_DISCUSSION);
            String commentTable = findNamedTableIgnoreCase(connection, TABLE_GDN_COMMENT);
            String userTable = findNamedTableIgnoreCase(connection, TABLE_GDN_USER);
            if (discussionTable == null || commentTable == null) {
                JsonObject payload = Json.createObjectBuilder()
                        .add(KEY_STATUS, VALUE_OK)
                        .add(KEY_REPLY_COUNT, 0)
                        .add(KEY_REPLIES, Json.createArrayBuilder())
                        .build();
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            TableInfo discussionInfo = loadTableInfo(connection, dbType, discussionTable);
            TableInfo commentInfo = loadTableInfo(connection, dbType, commentTable);
            TableInfo userInfo = userTable == null ? null : loadTableInfo(connection, dbType, userTable);
            String discussionIdCol = detectFirstColumn(discussionInfo.columns,
                List.of(TOKEN_DISCUSSION_ID, TOKEN_DISCUSSIONID, TOKEN_ID));
            String commentBodyCol = detectFirstColumn(commentInfo.columns,
                        List.of(TOKEN_BODY, TOKEN_COMMENT, TOKEN_MESSAGE, TOKEN_CONTENT, TOKEN_TEXT));
            String commentDiscussionIdCol = detectFirstColumn(commentInfo.columns,
                List.of(TOKEN_DISCUSSION_ID, TOKEN_DISCUSSIONID, TOKEN_CONVERSATION_ID, TOKEN_CONVERSATIONID));
            String userIdCol = userInfo == null ? null
                : detectFirstColumn(userInfo.columns, List.of(TOKEN_USER_ID, TOKEN_USERID, TOKEN_ID));
            String userNameCol = userInfo == null ? null
                : detectFirstColumn(userInfo.columns, List.of(TOKEN_NAME, TOKEN_USERNAME, TOKEN_FULL_NAME, TOKEN_DISPLAY_NAME));
            if (discussionIdCol == null || commentBodyCol == null || commentDiscussionIdCol == null) {
                JsonObject payload = Json.createObjectBuilder()
                        .add(KEY_STATUS, VALUE_OK)
                        .add(KEY_REPLY_COUNT, 0)
                        .add(KEY_REPLIES, Json.createArrayBuilder())
                        .build();
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

        String commentActorCol = detectFirstColumn(commentInfo.columns,
            List.of(TOKEN_INSERT_USER_ID, TOKEN_INSERTUSERID, TOKEN_USER_ID, TOKEN_USERID, TOKEN_CREATED_BY, TOKEN_AUTHOR_ID, TOKEN_SENDER_ID));
        String commentCreatedAtCol = detectFirstColumn(commentInfo.columns,
            List.of(TOKEN_DATE_INSERTED, TOKEN_DATEINSERTED, TOKEN_CREATED_AT, TOKEN_CREATEDON, TOKEN_CREATED, TOKEN_TIMESTAMP, TOKEN_INSERTED_AT));
        String commentIdCol = detectFirstColumn(commentInfo.columns,
            List.of(TOKEN_COMMENT_ID, TOKEN_COMMENTID, TOKEN_ID));

        String quotedDiscussionTable = quoteIdentifier(dbType, discussionTable);
        String quotedCommentTable = quoteIdentifier(dbType, commentTable);
        String actorExpr = commentActorCol == null ? SQL_NULL_LITERAL : dbType.castToTextExpression(quoteQualified(dbType, ALIAS_COMMENT, commentActorCol));
        String userJoinSql = EMPTY;
        if (commentActorCol != null && userTable != null && userIdCol != null && userNameCol != null) {
            String commentActorTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_COMMENT, commentActorCol));
            String userIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_USER, userIdCol));
            String userNameTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_USER, userNameCol));
            actorExpr = SQL_COALESCE_NULLIF_TRIM_PREFIX + userNameTextExpr + SQL_EMPTY_FALLBACK_INFIX + commentActorTextExpr + ')';
            userJoinSql = SQL_LEFT_JOIN + quoteIdentifier(dbType, userTable) + SPACE + ALIAS_USER + SQL_ON
                + commentActorTextExpr + SQL_EQUALS_DELIMITER + userIdTextExpr + ' ';
        }
        String createdExpr = commentCreatedAtCol == null ? SQL_NULL_LITERAL : quoteQualified(dbType, ALIAS_COMMENT, commentCreatedAtCol);
        String bodyExpr = quoteQualified(dbType, ALIAS_COMMENT, commentBodyCol);

        String orderExpr = commentIdCol != null
            ? quoteQualified(dbType, ALIAS_COMMENT, commentIdCol)
            : (commentCreatedAtCol != null ? quoteQualified(dbType, ALIAS_COMMENT, commentCreatedAtCol) : bodyExpr);

        String sql = SQL_SELECT_PREFIX
            + actorExpr + SQL_AS_DELIMITER + KEY_ACTOR + COMMA_SPACE
            + '\'' + VALUE_REPLY + '\'' + SQL_AS_DELIMITER + KEY_ROLE + COMMA_SPACE
            + createdExpr + SQL_AS_DELIMITER + TOKEN_CREATED_AT + COMMA_SPACE
                + bodyExpr + SQL_AS_DELIMITER + KEY_MESSAGE_TEXT + ' '
            + SQL_FROM + quotedCommentTable + SPACE + ALIAS_COMMENT + SPACE
            + SQL_JOIN + quotedDiscussionTable + SPACE + ALIAS_DISCUSSION + SQL_ON
                + quoteQualified(dbType, ALIAS_COMMENT, commentDiscussionIdCol) + SQL_EQUALS_DELIMITER + quoteQualified(dbType, ALIAS_DISCUSSION, discussionIdCol) + ' '
                + userJoinSql
            + SQL_WHERE + quoteQualified(dbType, ALIAS_DISCUSSION, discussionIdCol) + SQL_EQUALS_DELIMITER + SQL_PARAM_WITH_TRAILING_SPACE
                + SQL_ORDER_BY + orderExpr + SQL_ASC
                + SQL_LIMIT_QMARK;

        JsonArrayBuilder replies = Json.createArrayBuilder();
        int count = 0;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, discussionId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    addJsonCell(row, KEY_ACTOR, rs.getObject(KEY_ACTOR));
                    addJsonCell(row, KEY_ROLE, rs.getObject(KEY_ROLE));
                    addJsonCell(row, TOKEN_CREATED_AT, rs.getObject(TOKEN_CREATED_AT));
                    addJsonCell(row, KEY_MESSAGE_TEXT, rs.getObject(KEY_MESSAGE_TEXT));
                    replies.add(row);
                    count++;
                }
            }
        }

            JsonObject payload = Json.createObjectBuilder()
                    .add(KEY_STATUS, VALUE_OK)
                        .add(KEY_REPLY_COUNT, count)
                        .add(KEY_REPLIES, replies)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_QUERY_FORUM_THREAD_REPLIES, ex);
        }
    }

    private QueryResult tryRunGdnForumThreadQuery(Connection connection,
            DbType dbType,
            String search,
            int limit,
            int offset) {
        String discussionTable = findNamedTableIgnoreCase(connection, TABLE_GDN_DISCUSSION);
        String commentTable = findNamedTableIgnoreCase(connection, TABLE_GDN_COMMENT);
        String userTable = findNamedTableIgnoreCase(connection, TABLE_GDN_USER);
        String tagDiscussionTable = findNamedTableIgnoreCase(connection, TABLE_GDN_TAG_DISCUSSION);
        String tagTable = findNamedTableIgnoreCase(connection, TABLE_GDN_TAG);

        String statusTable = resolveStatusTableName(connection);

        if (discussionTable == null) {
            return null;
        }

        TableInfo discussionInfo = loadTableInfo(connection, dbType, discussionTable);
        TableInfo userInfo = userTable == null ? null : loadTableInfo(connection, dbType, userTable);
        TableInfo tagDiscussionInfo = tagDiscussionTable == null ? null : loadTableInfo(connection, dbType, tagDiscussionTable);
        TableInfo tagInfo = tagTable == null ? null : loadTableInfo(connection, dbType, tagTable);
        TableInfo statusInfo = statusTable == null ? null : loadTableInfo(connection, dbType, statusTable);

        String discussionIdCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_DISCUSSION_ID, TOKEN_DISCUSSIONID, TOKEN_ID));
        String subjectCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_NAME, TOKEN_TITLE, TOKEN_SUBJECT));
        String discussionBodyCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_BODY, TOKEN_MESSAGE, TOKEN_CONTENT, TOKEN_TEXT));
        String discussionActorCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_INSERT_USER_ID, TOKEN_INSERTUSERID, TOKEN_USER_ID, TOKEN_USERID, TOKEN_CREATED_BY, TOKEN_AUTHOR_ID, TOKEN_SENDER_ID));
        String discussionCreatedAtCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_DATE_INSERTED, TOKEN_DATEINSERTED, TOKEN_CREATED_AT, TOKEN_CREATEDON, TOKEN_CREATED, TOKEN_TIMESTAMP, TOKEN_INSERTED_AT));
        String discussionStatusIdCol = detectFirstColumn(discussionInfo.columns,
            List.of(KEY_STATUS_ID, TOKEN_STATUSID, TOKEN_RECORD_STATUS_ID, TOKEN_RECORDSTATUSID));
        String discussionQnaCol = detectFirstColumn(discussionInfo.columns,
            List.of(TOKEN_QNA, TOKEN_QUESTION_STATUS, KEY_QNA_STATUS));

        String tagDiscussionIdCol = tagDiscussionInfo == null ? null : detectFirstColumn(tagDiscussionInfo.columns,
            List.of(TOKEN_DISCUSSION_ID, TOKEN_DISCUSSIONID, TOKEN_RECORD_ID, TOKEN_RECORDID));
        String tagDiscussionTagIdCol = tagDiscussionInfo == null ? null : detectFirstColumn(tagDiscussionInfo.columns,
            List.of(TOKEN_TAG_ID, TOKEN_TAGID));
        String tagIdCol = tagInfo == null ? null : detectFirstColumn(tagInfo.columns,
            List.of(TOKEN_TAG_ID, TOKEN_TAGID, TOKEN_ID));
        String tagNameCol = tagInfo == null ? null : detectFirstColumn(tagInfo.columns,
            List.of(TOKEN_FULLNAME, TOKEN_NAME, TOKEN_TAG));

        String statusIdCol = statusInfo == null ? null : detectFirstColumn(statusInfo.columns,
            List.of(KEY_STATUS_ID, TOKEN_STATUSID, TOKEN_RECORD_STATUS_ID, TOKEN_RECORDSTATUSID, TOKEN_ID));
        String statusNameCol = statusInfo == null ? null : detectFirstColumn(statusInfo.columns,
            List.of(TOKEN_NAME, TOKEN_LABEL, KEY_STATUS));
        String userIdCol = userInfo == null ? null
            : detectFirstColumn(userInfo.columns, List.of(TOKEN_USER_ID, TOKEN_USERID, TOKEN_ID));
        String userNameCol = userInfo == null ? null
            : detectFirstColumn(userInfo.columns, List.of(TOKEN_NAME, TOKEN_USERNAME, TOKEN_FULL_NAME, TOKEN_DISPLAY_NAME));

        if (discussionIdCol == null || discussionBodyCol == null) {
            return null;
        }

        String quotedDiscussionTable = quoteIdentifier(dbType, discussionTable);

        String dDiscussionIdExpr = quoteQualified(dbType, ALIAS_DISCUSSION, discussionIdCol);
        String dSubjectExpr = subjectCol == null ? SQL_NULL_LITERAL : quoteQualified(dbType, ALIAS_DISCUSSION, subjectCol);
        String dBodyExpr = quoteQualified(dbType, ALIAS_DISCUSSION, discussionBodyCol);
        String[] actorProjection = resolveDiscussionActorProjection(
            dbType,
            discussionActorCol,
            userTable,
            userIdCol,
            userNameCol
        );
        String dActorExpr = actorProjection[0];
        String userJoinSql = actorProjection[1];
        String dStatusIdExpr = discussionStatusIdCol == null ? SQL_NULL_LITERAL : quoteQualified(dbType, ALIAS_DISCUSSION, discussionStatusIdCol);
        String dQnaExpr = discussionQnaCol == null ? SQL_NULL_LITERAL : quoteQualified(dbType, ALIAS_DISCUSSION, discussionQnaCol);
        String[] statusProjection = resolveStatusProjection(
            dbType,
            statusTable,
            discussionStatusIdCol,
            statusIdCol,
            statusNameCol
        );
        String statusNameExpr = statusProjection[0];
        String statusJoinSql = statusProjection[1];

        String tagNamesExpr = resolveTagNamesProjection(
            dbType,
            tagDiscussionTable,
            tagTable,
            tagDiscussionIdCol,
            tagDiscussionTagIdCol,
            tagIdCol,
            tagNameCol,
            dDiscussionIdExpr
        );

        String dCreatedExpr = discussionCreatedAtCol == null ? SQL_NULL_LITERAL : quoteQualified(dbType, ALIAS_DISCUSSION, discussionCreatedAtCol);

        String originalSelect = SQL_SELECT_PREFIX
            + dDiscussionIdExpr + SQL_AS_DELIMITER + KEY_MESSAGE_ID + COMMA_SPACE
            + dDiscussionIdExpr + SQL_AS_DELIMITER + TOKEN_CONVERSATION_ID + COMMA_SPACE
            + dSubjectExpr + SQL_AS_DELIMITER + TOKEN_SUBJECT + COMMA_SPACE
            + dActorExpr + SQL_AS_DELIMITER + KEY_ACTOR + COMMA_SPACE
                + '\'' + VALUE_USER + '\'' + SQL_AS_DELIMITER + KEY_ROLE + COMMA_SPACE
            + dCreatedExpr + SQL_AS_DELIMITER + TOKEN_CREATED_AT + COMMA_SPACE
            + dBodyExpr + SQL_AS_DELIMITER + KEY_MESSAGE_TEXT + COMMA_SPACE
                + SQL_NULL_LITERAL + SQL_AS_DELIMITER + TOKEN_RESPONSE_TEXT + COMMA_SPACE
                + dStatusIdExpr + SQL_AS_DELIMITER + KEY_STATUS_ID + COMMA_SPACE
                + statusNameExpr + SQL_AS_DELIMITER + KEY_STATUS_NAME + COMMA_SPACE
                + dQnaExpr + SQL_AS_DELIMITER + KEY_QNA_STATUS + COMMA_SPACE
                + tagNamesExpr + SQL_AS_DELIMITER + KEY_TAG_NAMES + COMMA_SPACE
                + dDiscussionIdExpr + SQL_AS_DELIMITER + TOKEN_CHAIN_KEY + ' '
                + SQL_FROM + quotedDiscussionTable + SPACE + ALIAS_DISCUSSION
                + userJoinSql
                + statusJoinSql;

        String wrappedFrom = SQL_FROM_OPEN + originalSelect + SQL_FORUM_ROWS_ALIAS;
        String trimmedSearch = search == null ? EMPTY : search.trim();
        boolean hasSearch = !trimmedSearch.isEmpty();
        String searchWhere = hasSearch
            ? SQL_WHERE_OPEN
                        + SQL_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, TOKEN_SUBJECT)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, TOKEN_CONVERSATION_ID)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_ACTOR)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_MESSAGE_TEXT)) + SQL_LIKE_PARAM_SUFFIX
                    + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_MESSAGE_ID)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_STATUS_NAME)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_QNA_STATUS)) + SQL_LIKE_PARAM_SUFFIX
                        + SQL_OR_LOWER_COALESCE_PREFIX + dbType.castToTextExpression(quoteIdentifier(dbType, KEY_TAG_NAMES)) + SQL_LIKE_PARAM_SUFFIX
                        + ')'
                : EMPTY;

        try {
            String countSql = SQL_SELECT_COUNT + wrappedFrom + searchWhere;
            int total = 0;
            try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
                if (hasSearch) {
                    bindLikeParams(countStmt, 1, trimmedSearch, 8);
                }
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

        String qnaTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, KEY_QNA_STATUS));
        String statusNameTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, KEY_STATUS_NAME));
        String tagNamesTextExpr = dbType.castToTextExpression(quoteIdentifier(dbType, KEY_TAG_NAMES));
        String communityConfirmedExpr = SQL_CASE
            + SQL_WHEN_LOWER_COALESCE_PREFIX + qnaTextExpr + SQL_ANSWERED_ACCEPTED_THEN
            + SQL_WHEN_LOWER_COALESCE_PREFIX + statusNameTextExpr + SQL_ANSWERED_ACCEPTED_THEN
            + SQL_WHEN_LOWER_COALESCE_PREFIX + tagNamesTextExpr + SQL_LIKE_ANSWERED
            + SQL_WHEN_LOWER_COALESCE_PREFIX + tagNamesTextExpr + SQL_LIKE_ACCEPTED
            + SQL_ELSE_ZERO_END_AS + quoteIdentifier(dbType, TOKEN_COMMUNITY_CONFIRMED);

        String dataSql = SQL_SELECT_PREFIX
            + quoteIdentifier(dbType, KEY_MESSAGE_ID) + ','
                + quoteIdentifier(dbType, TOKEN_CONVERSATION_ID) + ','
            + quoteIdentifier(dbType, TOKEN_SUBJECT) + ','
                + quoteIdentifier(dbType, KEY_ACTOR) + ','
            + quoteIdentifier(dbType, KEY_ROLE) + ','
            + quoteIdentifier(dbType, TOKEN_CREATED_AT) + ','
                + quoteIdentifier(dbType, KEY_MESSAGE_TEXT) + ','
            + quoteIdentifier(dbType, TOKEN_RESPONSE_TEXT) + ','
                + quoteIdentifier(dbType, KEY_STATUS_ID) + ','
                + quoteIdentifier(dbType, KEY_STATUS_NAME) + ','
                + quoteIdentifier(dbType, KEY_QNA_STATUS) + ','
                + quoteIdentifier(dbType, KEY_TAG_NAMES) + ','
            + quoteIdentifier(dbType, TOKEN_CHAIN_KEY) + ','
                + communityConfirmedExpr
                + wrappedFrom
                + searchWhere
            + SQL_ORDER_BY + quoteIdentifier(dbType, TOKEN_CREATED_AT) + SQL_DESC
                + SQL_LIMIT_OFFSET;

            JsonArrayBuilder rows = Json.createArrayBuilder();
            int returned = 0;
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                int idx = 1;
                if (hasSearch) {
                    idx = bindLikeParams(statement, idx, trimmedSearch, 8);
                }
                statement.setInt(idx++, limit);
                statement.setInt(idx, offset);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder row = Json.createObjectBuilder();
                        addJsonCell(row, KEY_MESSAGE_ID, rs.getObject(KEY_MESSAGE_ID));
                        addJsonCell(row, TOKEN_CONVERSATION_ID, rs.getObject(TOKEN_CONVERSATION_ID));
                        addJsonCell(row, TOKEN_SUBJECT, rs.getObject(TOKEN_SUBJECT));
                        addJsonCell(row, KEY_ACTOR, rs.getObject(KEY_ACTOR));
                        addJsonCell(row, KEY_ROLE, rs.getObject(KEY_ROLE));
                        addJsonCell(row, TOKEN_CREATED_AT, rs.getObject(TOKEN_CREATED_AT));
                        addJsonCell(row, KEY_MESSAGE_TEXT, rs.getObject(KEY_MESSAGE_TEXT));
                        addJsonCell(row, TOKEN_RESPONSE_TEXT, rs.getObject(TOKEN_RESPONSE_TEXT));
                        addJsonCell(row, KEY_STATUS_ID, rs.getObject(KEY_STATUS_ID));
                        addJsonCell(row, KEY_STATUS_NAME, rs.getObject(KEY_STATUS_NAME));
                        addJsonCell(row, KEY_QNA_STATUS, rs.getObject(KEY_QNA_STATUS));
                        addJsonCell(row, KEY_TAG_NAMES, rs.getObject(KEY_TAG_NAMES));
                        addJsonCell(row, TOKEN_CHAIN_KEY, rs.getObject(TOKEN_CHAIN_KEY));
                        addJsonCell(row, TOKEN_COMMUNITY_CONFIRMED, rs.getObject(TOKEN_COMMUNITY_CONFIRMED));
                        rows.add(row);
                        returned++;
                    }
                }
            }

            JsonArrayBuilder columns = Json.createArrayBuilder();
            columns.add(KEY_MESSAGE_ID);
            columns.add(TOKEN_CONVERSATION_ID);
            columns.add(TOKEN_SUBJECT);
            columns.add(KEY_ACTOR);
            columns.add(KEY_ROLE);
            columns.add(TOKEN_CREATED_AT);
            columns.add(KEY_MESSAGE_TEXT);
            columns.add(TOKEN_RESPONSE_TEXT);
            columns.add(KEY_STATUS_ID);
            columns.add(KEY_STATUS_NAME);
            columns.add(KEY_QNA_STATUS);
            columns.add(KEY_TAG_NAMES);
            columns.add(TOKEN_CHAIN_KEY);
            columns.add(TOKEN_COMMUNITY_CONFIRMED);

            return new QueryResult(
                    ENTITY_MESSAGES,
                        joinTablesForProjection(discussionTable, commentTable, userTable, statusTable, tagDiscussionTable, tagTable),
                    limit,
                    offset,
                    returned,
                    total,
                    columns,
                    rows,
                        KEY_MESSAGE_TEXT,
                    TOKEN_RESPONSE_TEXT,
                        KEY_ACTOR,
                    KEY_ROLE,
                        TOKEN_CONVERSATION_ID,
                    TOKEN_CREATED_AT
            );
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_RUN_DISCUSSION_THREAD_PROJECTION_QUERY, ex);
        }
    }

    private String resolveStatusTableName(Connection connection) {
        String statusTable = findNamedTableIgnoreCase(connection, TABLE_GDN_RECORD_STATUS);
        if (statusTable != null) {
            return statusTable;
        }

        statusTable = findNamedTableIgnoreCase(connection, TABLE_RECORD_STATUS);
        if (statusTable != null) {
            return statusTable;
        }

        return findNamedTableIgnoreCase(connection, TABLE_GDN_STATUS);
    }

    private String[] resolveDiscussionActorProjection(DbType dbType,
            String discussionActorCol,
            String userTable,
            String userIdCol,
            String userNameCol) {
        String actorExpr = discussionActorCol == null
            ? SQL_NULL_LITERAL
            : dbType.castToTextExpression(quoteQualified(dbType, ALIAS_DISCUSSION, discussionActorCol));
        String userJoinSql = EMPTY;

        if (discussionActorCol != null && userTable != null && userIdCol != null && userNameCol != null) {
            String discussionActorTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_DISCUSSION, discussionActorCol));
            String userIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_USER, userIdCol));
            String userNameTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_USER, userNameCol));
            actorExpr = SQL_COALESCE_NULLIF_TRIM_PREFIX + userNameTextExpr + SQL_EMPTY_FALLBACK_INFIX + discussionActorTextExpr + ')';
            userJoinSql = SQL_LEFT_JOIN + quoteIdentifier(dbType, userTable) + SPACE + ALIAS_USER + SQL_ON
                + discussionActorTextExpr + SQL_EQUALS_DELIMITER + userIdTextExpr + ' ';
        }

        return new String[]{actorExpr, userJoinSql};
    }

    private String[] resolveStatusProjection(DbType dbType,
            String statusTable,
            String discussionStatusIdCol,
            String statusIdCol,
            String statusNameCol) {
        String statusNameExpr = SQL_NULL_LITERAL;
        String statusJoinSql = EMPTY;

        if (statusTable != null
            && discussionStatusIdCol != null
            && statusIdCol != null
            && statusNameCol != null) {
            String discussionStatusTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_DISCUSSION, discussionStatusIdCol));
            String statusIdTextExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_RS, statusIdCol));
            statusJoinSql = SQL_LEFT_JOIN + quoteIdentifier(dbType, statusTable) + SPACE + ALIAS_RS + SQL_ON
                + discussionStatusTextExpr + SQL_EQUALS_DELIMITER + statusIdTextExpr + ' ';
            statusNameExpr = quoteQualified(dbType, ALIAS_RS, statusNameCol);
        }

        return new String[]{statusNameExpr, statusJoinSql};
    }

    private String resolveTagNamesProjection(DbType dbType,
            String tagDiscussionTable,
            String tagTable,
            String tagDiscussionIdCol,
            String tagDiscussionTagIdCol,
            String tagIdCol,
            String tagNameCol,
            String discussionIdExpr) {
        if (tagDiscussionTable == null
            || tagTable == null
            || tagDiscussionIdCol == null
            || tagDiscussionTagIdCol == null
            || tagIdCol == null
            || tagNameCol == null) {
            return SQL_NULL_LITERAL;
        }

        return buildTagNamesExpression(
            dbType,
            tagDiscussionTable,
            tagTable,
            tagDiscussionIdCol,
            tagDiscussionTagIdCol,
            tagIdCol,
            tagNameCol,
            discussionIdExpr
        );
    }

    private String joinTablesForProjection(String... tableNames) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                if (tableName == null || tableName.isBlank()) {
                    continue;
                }
                ordered.add(tableName);
            }
        }
        if (ordered.isEmpty()) {
            return EMPTY;
        }
        return String.join(VALUE_PLUS, ordered);
    }

    private String buildTagNamesExpression(DbType dbType,
            String tagDiscussionTable,
            String tagTable,
            String tagDiscussionIdCol,
            String tagDiscussionTagIdCol,
            String tagIdCol,
            String tagNameCol,
            String discussionIdExpr) {
        String tdDiscussionExpr = quoteQualified(dbType, ALIAS_TD, tagDiscussionIdCol);
        String tdTagIdExpr = quoteQualified(dbType, ALIAS_TD, tagDiscussionTagIdCol);
        String tgTagIdExpr = quoteQualified(dbType, ALIAS_TG, tagIdCol);
        String tgNameExpr = dbType.castToTextExpression(quoteQualified(dbType, ALIAS_TG, tagNameCol));

        String aggregate;
        if (dbType == DbType.MYSQL) {
            aggregate = SQL_GROUP_CONCAT_DISTINCT + tgNameExpr + SQL_ORDER_BY + tgNameExpr + SQL_GROUP_CONCAT_SEPARATOR;
        } else {
            aggregate = SQL_STRING_AGG_DISTINCT + tgNameExpr + SQL_STRING_AGG_SEPARATOR;
        }

        return SQL_SELECT_OPEN
                + aggregate
            + SQL_FROM + quoteIdentifier(dbType, tagDiscussionTable) + SPACE + ALIAS_TD
            + SPACE + SQL_JOIN + quoteIdentifier(dbType, tagTable) + SPACE + ALIAS_TG + SQL_ON + tdTagIdExpr + SQL_EQUALS_DELIMITER + tgTagIdExpr
            + SPACE + SQL_WHERE + tdDiscussionExpr + SQL_EQUALS_DELIMITER + discussionIdExpr
                + ')';
    }

    private static int bindLikeParams(PreparedStatement statement, int startIndex, String rawSearch, int count) {
        int idx = startIndex;
        String likeValue = '%' + (rawSearch == null ? EMPTY : rawSearch.toLowerCase(Locale.ROOT)) + '%';
        try {
            for (int i = 0; i < count; i++) {
                statement.setString(idx++, likeValue);
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_BIND_SEARCH_PARAMS, ex);
        }
        return idx;
    }

    private String quoteQualified(DbType dbType, String alias, String column) {
        return alias + '.' + quoteIdentifier(dbType, column);
    }

    private String findNamedTableIgnoreCase(Connection connection, String wantedTableName) {
        if (wantedTableName == null || wantedTableName.isBlank()) {
            return null;
        }

        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add(SCHEMA_PUBLIC);
            schemaCandidates.add(null);

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, WILDCARD_ALL, new String[]{METADATA_TABLE_TYPE})) {
                    while (rs.next()) {
                        String tableName = rs.getString(TABLE_NAME_COLUMN);
                        if (tableName != null && tableName.equalsIgnoreCase(wantedTableName)) {
                            return tableName;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_LOCATE_TABLE_PREFIX + wantedTableName + '.', ex);
        }

        return null;
    }

    private String findAlternativeMessageTableWithRows(Connection connection,
            DbType dbType,
            String currentTable) {
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add(SCHEMA_PUBLIC);
            schemaCandidates.add(null);

            String bestTable = null;
            int bestScore = Integer.MIN_VALUE;

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, WILDCARD_ALL, new String[]{METADATA_TABLE_TYPE})) {
                    while (rs.next()) {
                        String tableName = rs.getString(TABLE_NAME_COLUMN);
                        if (tableName == null || tableName.isBlank()) {
                            continue;
                        }
                        if (currentTable != null && tableName.equalsIgnoreCase(currentTable)) {
                            continue;
                        }

                        List<String> columns = loadTableColumns(md, catalog, schema, tableName);
                        int score = scoreRelaxedMessageTable(tableName, columns);
                        if (score < 120) {
                            continue;
                        }
                        if (!tableHasAtLeastOneRow(connection, dbType, tableName)) {
                            continue;
                        }

                        if (bestTable == null || score > bestScore) {
                            bestTable = tableName;
                            bestScore = score;
                        }
                    }
                }
            }

            return bestTable;
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_FIND_ALTERNATE_MESSAGES_TABLE, ex);
        }
    }

    private boolean tableHasAtLeastOneRow(Connection connection, DbType dbType, String tableName) {
        String sql = SQL_SELECT_ONE_FROM + quoteIdentifier(dbType, tableName) + SQL_LIMIT_ONE;
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOG.log(Level.FINE, MSG_SKIPPING_CANDIDATE_TABLE_PREFIX + tableName, ex);
            return false;
        }
    }

    private static ColumnHints detectColumnHints(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return EMPTY_COLUMN_HINTS;
        }

        String messageTextColumn = detectFirstColumn(columns,
            List.of(TOKEN_PROMPT, TOKEN_MESSAGE, TOKEN_BODY, TOKEN_CONTENT, TOKEN_TEXT, TOKEN_QUESTION, TOKEN_USER_MESSAGE, TOKEN_USER_TEXT, TOKEN_POST_TEXT));
        String responseTextColumn = detectFirstColumn(columns,
            List.of(TOKEN_RESPONSE_TEXT, TOKEN_RESPONSE, TOKEN_ASSISTANT_RESPONSE, VALUE_REPLY, TOKEN_ANSWER, TOKEN_BOT_RESPONSE));
        String actorColumn = detectFirstColumn(columns,
            List.of(
                TOKEN_USERNAME,
                TOKEN_USER_NAME,
                TOKEN_DISPLAY_NAME,
                TOKEN_AUTHOR_NAME,
                TOKEN_SENDER_NAME,
                TOKEN_FULL_NAME,
                TOKEN_INSERT_USER_ID,
                TOKEN_INSERTUSERID,
                TOKEN_USER_ID,
                TOKEN_AUTHOR_ID,
                TOKEN_SENDER_ID,
                TOKEN_CREATED_BY,
                TOKEN_CREATED_BY_USER,
                TOKEN_OWNER_ID,
                VALUE_USER,
                TOKEN_AUTHOR,
                TOKEN_SENDER
            ));
        String roleColumn = detectFirstColumn(columns,
            List.of(KEY_ROLE, TOKEN_SENDER_TYPE, TOKEN_AUTHOR_TYPE, TOKEN_MESSAGE_TYPE, TOKEN_ORIGIN, TOKEN_SOURCE, TOKEN_DIRECTION));
        String conversationColumn = detectFirstColumn(columns,
            List.of(
                TOKEN_SESSION_ID,
                TOKEN_SESSIONID,
                TOKEN_SESSION,
                TOKEN_CONVERSATION_ID,
                TOKEN_CONVERSATIONID,
                TOKEN_CONVERSATION,
                TOKEN_AI_CONVERSATION_ID,
                TOKEN_AICONVERSATIONID,
                TOKEN_THREAD_ID,
                TOKEN_THREADID,
                TOKEN_THREAD,
                TOKEN_CHAT_ID,
                TOKEN_CHATID,
                TOKEN_CHAT,
                TOKEN_DIALOG_ID,
                TOKEN_DIALOGID,
                TOKEN_DIALOG,
                TOKEN_DISCUSSION_ID,
                TOKEN_DISCUSSIONID,
                TOKEN_DISCUSSION,
                TOKEN_TOPIC_ID,
                TOKEN_TOPICID,
                TOKEN_TOPIC,
                TOKEN_WIDGET_CHAT_ID,
                TOKEN_WIDGETCHATID,
                TOKEN_CONVERSATION_KEY,
                TOKEN_SESSION_KEY
            ));
        String createdAtColumn = detectFirstColumn(columns,
            List.of(
                TOKEN_DATE_INSERTED,
                TOKEN_DATEINSERTED,
                TOKEN_CREATED_AT,
                TOKEN_CREATED_ON,
                TOKEN_CREATED,
                TOKEN_CREATEDON,
                TOKEN_CREATEDAT,
                TOKEN_TIMESTAMP,
                TOKEN_SENT_AT,
                TOKEN_MESSAGE_TIME,
                TOKEN_EVENT_TIME,
                TOKEN_POSTED_AT,
                TOKEN_INSERTED_AT,
                TOKEN_INSERTED,
                TOKEN_TIME,
                TOKEN_DATE
            ));

        if (messageTextColumn == null) {
            messageTextColumn = detectFallbackMessageColumn(columns);
        }

        return new ColumnHints(
                messageTextColumn,
                responseTextColumn,
                actorColumn,
                roleColumn,
                conversationColumn,
                createdAtColumn
        );
    }

    private static String detectFirstColumn(List<String> columns, List<String> candidates) {
        if (columns == null || columns.isEmpty() || candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (String candidate : candidates) {
            for (String col : columns) {
                if (col != null && col.equalsIgnoreCase(candidate)) {
                    return col;
                }
            }
        }

        for (String candidate : candidates) {
            String normalizedCandidate = normalizeNameToken(candidate);
            for (String col : columns) {
                if (col == null) {
                    continue;
                }
                String normalizedColumn = normalizeNameToken(col);
                if (!normalizedColumn.isEmpty() && normalizedColumn.contains(normalizedCandidate)) {
                    return col;
                }
            }
        }

        return null;
    }

    private static String detectFallbackMessageColumn(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        for (String col : columns) {
            if (col == null || col.isBlank()) {
                continue;
            }
            String normalized = normalizeNameToken(col);
            if (normalized.endsWith(TOKEN_ID) || normalized.equals(TOKEN_ID) || normalized.contains(TOKEN_TIME) || normalized.contains(TOKEN_DATE)) {
                continue;
            }
            return col;
        }
        return null;
    }

    private int bindSearchParams(PreparedStatement statement,
            List<String> searchableColumns,
            String search,
            boolean hasSearch,
            DbType dbType) {
        int idx = 1;
        if (!hasSearch) {
            return idx;
        }
        String likeValue = '%' + search.toLowerCase(Locale.ROOT) + '%';
        int columnCount = searchableColumns.size();
        try {
            for (int i = 0; i < columnCount; i++) {
                statement.setString(idx++, likeValue);
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_BIND_TABLE_SEARCH_PARAMS, ex);
        }
        return idx;
    }

    private String buildWhereClause(DbType dbType, List<String> searchableColumns, boolean hasSearch) {
        if (!hasSearch) {
            return EMPTY;
        }

        StringBuilder where = new StringBuilder(SQL_WHERE_OPEN);
        for (int i = 0; i < searchableColumns.size(); i++) {
            if (i > 0) {
                where.append(SQL_OR);
            }
            String quoted = quoteIdentifier(dbType, searchableColumns.get(i));
                where.append(SQL_LOWER_COALESCE_PREFIX)
                    .append(dbType.castToTextExpression(quoted))
                    .append(SQL_LIKE_PARAM_SUFFIX);
        }
        where.append(')');
        return where.toString();
    }

    private TableInfo loadTableInfo(Connection connection, DbType dbType, String tableName) {
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();
            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add(SCHEMA_PUBLIC);
            schemaCandidates.add(null);

            List<ColumnTypeInfo> typedColumns = new ArrayList<>();
            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getColumns(catalog, schema, tableName, WILDCARD_ALL)) {
                    while (rs.next()) {
                        String columnName = rs.getString(COLUMN_NAME_COLUMN);
                        if (columnName == null || columnName.isBlank()) {
                            continue;
                        }
                        int columnType = rs.getInt(TOKEN_DATA_TYPE);
                        String typeName = rs.getString(TOKEN_TYPE_NAME);
                        int ordinal = rs.getInt(TOKEN_ORDINAL_POSITION);
                        typedColumns.add(new ColumnTypeInfo(columnName, columnType, typeName, ordinal));
                    }
                }
                if (!typedColumns.isEmpty()) {
                    break;
                }
            }

            typedColumns.sort((left, right) -> Integer.compare(left.ordinalPosition, right.ordinalPosition));
            List<String> columns = new ArrayList<>();
            List<String> searchableColumns = new ArrayList<>();
            for (ColumnTypeInfo column : typedColumns) {
                columns.add(column.name);
                if (isTextLikeColumn(column.sqlType, column.sqlTypeName)) {
                    searchableColumns.add(column.name);
                }
            }
            return new TableInfo(columns, searchableColumns);
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX + tableName + '.', ex);
        }
    }

    private String joinQuotedColumns(DbType dbType, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(quoteIdentifier(dbType, columns.get(i)));
        }
        return sb.toString();
    }

    private static boolean isTextLikeColumn(int sqlType, String sqlTypeName) {
        if (sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NVARCHAR
                || sqlType == Types.LONGNVARCHAR
                || sqlType == Types.CLOB) {
            return true;
        }

        if (sqlTypeName == null) {
            return false;
        }
        String type = sqlTypeName.toLowerCase(Locale.ROOT);
        return type.contains(TOKEN_TEXT) || type.contains(TOKEN_CHAR) || type.contains(TOKEN_JSON) || type.contains(TOKEN_XML)
            || type.contains(TOKEN_UUID);
    }

    private String findTableName(Connection connection, String wantedEntity) {
        if (wantedEntity == null || wantedEntity.isBlank()) {
            return null;
        }
        try {
            DatabaseMetaData md = connection.getMetaData();
            String catalog = connection.getCatalog();
            List<String> probes = entityProbes(wantedEntity);

            Set<String> schemaCandidates = new LinkedHashSet<>();
            schemaCandidates.add(connection.getSchema());
            schemaCandidates.add(SCHEMA_PUBLIC);
            schemaCandidates.add(null);

            if (ENTITY_MESSAGES.equalsIgnoreCase(wantedEntity)) {
                String relaxedMessageTable = findRelaxedMessagesTable(md, catalog, schemaCandidates);
                if (relaxedMessageTable != null) {
                    return relaxedMessageTable;
                }
            }

            for (String schema : schemaCandidates) {
                String exact = findTableNameInSchema(md, catalog, schema, probes);
                if (exact != null) {
                    return exact;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_RESOLVE_TABLE_FOR_ENTITY_PREFIX + wantedEntity + '.', ex);
        }
    }

    private String findTableNameInSchema(DatabaseMetaData md,
            String catalog,
            String schema,
            List<String> probes) {
        try {
            for (String probe : probes) {
                try (ResultSet rs = md.getTables(catalog, schema, probe, new String[]{METADATA_TABLE_TYPE})) {
                    while (rs.next()) {
                        String tableName = rs.getString(TABLE_NAME_COLUMN);
                        if (tableName != null && matchesProbe(tableName, probe)) {
                            return tableName;
                        }
                    }
                }
            }

            // Fallback: find closest table name containing one of the probe tokens.
            Map<String, Integer> candidates = new LinkedHashMap<>();
            try (ResultSet rs = md.getTables(catalog, schema, WILDCARD_ALL, new String[]{METADATA_TABLE_TYPE})) {
                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME_COLUMN);
                    if (tableName == null || tableName.isBlank()) {
                        continue;
                    }
                    int score = scoreTableCandidate(tableName, probes);
                    if (score > 0) {
                        candidates.put(tableName, Integer.valueOf(score));
                    }
                }
            }

            String bestTable = null;
            int bestScore = 0;
            for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
                if (entry.getValue().intValue() > bestScore) {
                    bestScore = entry.getValue().intValue();
                    bestTable = entry.getKey();
                }
            }

            if (bestScore > 0) {
                return bestTable;
            }

            return null;
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_RESOLVE_TABLE_IN_SCHEMA, ex);
        }
    }

    private String findRelaxedMessagesTable(DatabaseMetaData md,
            String catalog,
            Set<String> schemaCandidates) {
        try {
            String bestTable = null;
            int bestScore = 0;

            for (String schema : schemaCandidates) {
                try (ResultSet rs = md.getTables(catalog, schema, WILDCARD_ALL, new String[]{METADATA_TABLE_TYPE})) {
                    while (rs.next()) {
                        String tableName = rs.getString(TABLE_NAME_COLUMN);
                        if (tableName == null || tableName.isBlank()) {
                            continue;
                        }

                        List<String> columns = loadTableColumns(md, catalog, schema, tableName);
                        int score = scoreRelaxedMessageTable(tableName, columns);
                        if (score > bestScore) {
                            bestScore = score;
                            bestTable = tableName;
                        }
                    }
                }
            }

            return bestScore >= 120 ? bestTable : null;
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_FIND_RELAXED_MESSAGES_TABLE, ex);
        }
    }

    private static List<String> loadTableColumns(DatabaseMetaData md,
            String catalog,
            String schema,
            String tableName) {
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = md.getColumns(catalog, schema, tableName, WILDCARD_ALL)) {
            while (rs.next()) {
                String col = rs.getString(COLUMN_NAME_COLUMN);
                if (col == null || col.isBlank()) {
                    continue;
                }
                columns.add(col);
            }
        } catch (SQLException ex) {
            throw dataAccess(MSG_UNABLE_TO_INSPECT_COLUMNS_FOR_TABLE_PREFIX + tableName + '.', ex);
        }
        return columns;
    }

    private static int scoreRelaxedMessageTable(String tableName, List<String> columns) {
        if (tableName == null || tableName.isBlank() || columns == null || columns.isEmpty()) {
            return 0;
        }

        String messageTextCol = detectFirstColumn(columns, RELAXED_MESSAGE_TEXT_COLUMNS);
        String responseCol = detectFirstColumn(columns, RELAXED_RESPONSE_COLUMNS);
        String actorCol = detectFirstColumn(columns, RELAXED_ACTOR_COLUMNS);
        String conversationCol = detectFirstColumn(columns, RELAXED_CONVERSATION_COLUMNS);
        String createdAtCol = detectFirstColumn(columns, RELAXED_CREATED_AT_COLUMNS);

        if (messageTextCol == null && responseCol == null) {
            return 0;
        }

        int score = 0;
        String normalizedTable = normalizeNameToken(tableName);

        for (String token : RELAXED_MESSAGE_NAME_TOKENS) {
            String normalizedToken = normalizeNameToken(token);
            if (!normalizedToken.isEmpty() && normalizedTable.contains(normalizedToken)) {
                score += 16;
            }
        }

        if (messageTextCol != null) {
            score += 90;
        }
        if (responseCol != null) {
            score += 45;
        }
        if (actorCol != null) {
            score += 30;
        }
        if (conversationCol != null) {
            score += 35;
        }
        if (createdAtCol != null) {
            score += 35;
        }

        if (messageTextCol != null && responseCol != null) {
            score += 25;
        }

        if (normalizedTable.contains(TOKEN_ARCHIVE)
            || normalizedTable.contains(TOKEN_BACKUP)
            || normalizedTable.contains(TOKEN_AUDIT)
            || normalizedTable.contains(TOKEN_HISTORY)) {
            score -= 40;
        }

        return score;
    }

    private static List<String> entityProbes(String entity) {
        String normalized = entity == null ? EMPTY : entity.toLowerCase(Locale.ROOT);
        List<String> aliases = ENTITY_TABLE_ALIASES.getOrDefault(normalized, List.of(normalized));
        List<String> probes = new ArrayList<>();
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            if (!probes.contains(alias)) {
                probes.add(alias);
            }
            String upper = alias.toUpperCase(Locale.ROOT);
            if (!probes.contains(upper)) {
                probes.add(upper);
            }
        }
        return probes;
    }

    private static boolean matchesProbe(String tableName, String probe) {
        return tableName.equalsIgnoreCase(probe);
    }

    private static int scoreTableCandidate(String tableName, List<String> probes) {
        String normalizedTable = normalizeNameToken(tableName);
        if (normalizedTable.isEmpty()) {
            return 0;
        }

        int best = 0;
        for (String probe : probes) {
            String normalizedProbe = normalizeNameToken(probe);
            if (normalizedProbe.isEmpty()) {
                continue;
            }

            if (normalizedTable.equals(normalizedProbe)) {
                return 100;
            }
            if (normalizedTable.startsWith(normalizedProbe)) {
                best = Math.max(best, 80);
            }
            if (normalizedTable.contains(normalizedProbe)) {
                best = Math.max(best, 60);
            }
        }
        return best;
    }

    private static String normalizeNameToken(String raw) {
        if (raw == null) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = Character.toLowerCase(raw.charAt(i));
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String quoteIdentifier(DbType dbType, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(MSG_INVALID_IDENTIFIER);
        }
        if (dbType == DbType.MYSQL) {
            return '`' + identifier.replace(VALUE_BACKTICK, VALUE_DOUBLE_BACKTICK) + '`';
        }
        return '"' + identifier.replace(VALUE_DOUBLE_QUOTE, VALUE_DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private static String normalizeParam(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace(VALUE_NUL_CHAR, EMPTY)
                .replace(System.lineSeparator(), EMPTY)
                .replace(System.lineSeparator(), EMPTY)
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeMultilineParam(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace(VALUE_NUL_CHAR, EMPTY)
                .replace(System.lineSeparator(), EMPTY)
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean isValidHost(String host) {
        return host != null && HOST_PATTERN.matcher(host).matches();
    }

    private static boolean isValidDatabaseName(String database) {
        return database != null && DATABASE_PATTERN.matcher(database).matches();
    }

    private static boolean isValidUsername(String username) {
        return username != null && USER_PATTERN.matcher(username).matches();
    }

    private static int parseIntWithBounds(String rawValue, int min, int max, String fieldName) {
        if (rawValue == null) {
            throw new IllegalArgumentException(fieldName + ERR_REQUIRED_SUFFIX_WITH_PERIOD);
        }
        try {
            int parsed = Integer.parseInt(rawValue);
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(fieldName + ERR_MUST_BE_BETWEEN + min + ERR_AND + max + '.');
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + ERR_MUST_BE_VALID_NUMBER, ex);
        }
    }

    private static ForumExplorerDataAccessException dataAccess(String message, SQLException ex) {
        return new ForumExplorerDataAccessException(message, ex);
    }

    private static boolean isAdmin(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? EMPTY : String.valueOf(roleObj);
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    private static void addJsonCell(JsonObjectBuilder row, String column, Object value) {
        if (value == null) {
            row.addNull(column);
            return;
        }
        if (value instanceof Boolean boolVal) {
            row.add(column, boolVal.booleanValue());
            return;
        }
        if (value instanceof byte[] bytes) {
            row.add(column, Base64.getEncoder().encodeToString(bytes));
            return;
        }
        if (value instanceof TemporalAccessor) {
            row.add(column, truncateCellValue(String.valueOf(value)));
            return;
        }
        row.add(column, truncateCellValue(String.valueOf(value)));
    }

    private static String truncateCellValue(String value) {
        if (value == null) {
            return EMPTY;
        }
        String normalized = value.replace(VALUE_NUL_CHAR, EMPTY);
        if (normalized.length() > MAX_CELL_TEXT_LEN) {
            return TextIoSanitizerUtil.safeSlice(normalized, 0, MAX_CELL_TEXT_LEN);
        }
        return normalized;
    }

    private static String safeErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return MSG_UNKNOWN_ERROR;
        }
        String msg = throwable.getMessage()
                .replace('\u0000', ' ')
                .replace(System.lineSeparator().charAt(0), ' ')
                .replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ')
                .trim();
        if (msg.length() > 300) {
            return TextIoSanitizerUtil.safeSlice(msg, 0, 300);
        }
        return msg;
    }

    private static void addOptionalString(JsonObjectBuilder builder, String key, String value) {
        if (builder == null || key == null || key.isBlank()) {
            return;
        }
        if (value == null || value.isBlank()) {
            builder.addNull(key);
            return;
        }
        builder.add(key, value);
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException ioe) {
            LOG.log(Level.FINE, MSG_UNABLE_TO_WRITE_EXPLORER_ERROR, ioe);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message);
                } catch (IOException ignored) {
                    LOG.log(Level.FINE, LOG_FALLBACK_SEND_ERROR_FAILED, ignored);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ioe) {
            LOG.log(Level.FINE, MSG_UNABLE_TO_WRITE_EXPLORER_JSON, ioe);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ignored) {
                    LOG.log(Level.FINE, LOG_FALLBACK_SEND_ERROR_FAILED, ignored);
                }
            }
        }
    }

    private enum DbType {
        POSTGRESQL(DEFAULT_DB_TYPE),
        MYSQL(DB_TYPE_MYSQL);

        private final String id;

        DbType(String id) {
            this.id = id;
        }

        private static DbType from(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (DB_TYPE_POSTGRES.equals(normalized) || DEFAULT_DB_TYPE.equals(normalized)) {
                return POSTGRESQL;
            }
            if (DB_TYPE_MYSQL.equals(normalized)) {
                return MYSQL;
            }
            return null;
        }

        private DataSource externalDataSource(String host, int port, String database, String username, String password) {
            if (this == MYSQL) {
                MysqlDataSource ds = new MysqlDataSource();
                ds.setServerName(host);
                ds.setPort(port);
                ds.setDatabaseName(database);
                ds.setUser(username);
                ds.setPassword(password);
                return ds;
            }

            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerNames(new String[]{host});
            ds.setPortNumbers(new int[]{port});
            ds.setDatabaseName(database);
            ds.setUser(username);
            ds.setPassword(password);
            return ds;
        }

        private String castToTextExpression(String quotedIdentifier) {
            if (this == MYSQL) {
                return SQL_CAST_PREFIX + quotedIdentifier + SQL_CAST_AS_CHAR;
            }
            return SQL_CAST_PREFIX + quotedIdentifier + SQL_CAST_AS_TEXT;
        }
    }

}

final class QueryResult {
    final String entity;
    final String tableName;
    final int limit;
    final int offset;
    final int returned;
    final int total;
    final JsonArrayBuilder columns;
    final JsonArrayBuilder rows;
    final String messageTextColumn;
    final String responseTextColumn;
    final String actorColumn;
    final String roleColumn;
    final String conversationColumn;
    final String createdAtColumn;

    QueryResult(String entity,
            String tableName,
            int limit,
            int offset,
            int returned,
            int total,
            JsonArrayBuilder columns,
            JsonArrayBuilder rows,
            String messageTextColumn,
            String responseTextColumn,
            String actorColumn,
            String roleColumn,
            String conversationColumn,
            String createdAtColumn) {
        this.entity = entity;
        this.tableName = tableName;
        this.limit = limit;
        this.offset = offset;
        this.returned = returned;
        this.total = total;
        this.columns = columns;
        this.rows = rows;
        this.messageTextColumn = messageTextColumn;
        this.responseTextColumn = responseTextColumn;
        this.actorColumn = actorColumn;
        this.roleColumn = roleColumn;
        this.conversationColumn = conversationColumn;
        this.createdAtColumn = createdAtColumn;
    }
}

final class ColumnHints {
    final String messageTextColumn;
    final String responseTextColumn;
    final String actorColumn;
    final String roleColumn;
    final String conversationColumn;
    final String createdAtColumn;

    ColumnHints(String messageTextColumn,
            String responseTextColumn,
            String actorColumn,
            String roleColumn,
            String conversationColumn,
            String createdAtColumn) {
        this.messageTextColumn = messageTextColumn;
        this.responseTextColumn = responseTextColumn;
        this.actorColumn = actorColumn;
        this.roleColumn = roleColumn;
        this.conversationColumn = conversationColumn;
        this.createdAtColumn = createdAtColumn;
    }
}

final class SavedConfig {
    final String dbType;
    final String host;
    final Integer port;
    final String database;
    final String username;
    final String passwordEnc;
    final boolean readableOnly;
    final String shareTo;
    final String shareSubject;
    final String updatedBy;
    final Timestamp updatedAt;

    SavedConfig(String dbType,
            String host,
            Integer port,
            String database,
            String username,
            String passwordEnc,
            boolean readableOnly,
            String shareTo,
            String shareSubject,
            String updatedBy,
            Timestamp updatedAt) {
        this.dbType = dbType;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.passwordEnc = passwordEnc;
        this.readableOnly = readableOnly;
        this.shareTo = shareTo;
        this.shareSubject = shareSubject;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}

final class ColumnTypeInfo {
    final String name;
    final int sqlType;
    final String sqlTypeName;
    final int ordinalPosition;

    ColumnTypeInfo(String name, int sqlType, String sqlTypeName, int ordinalPosition) {
        this.name = name;
        this.sqlType = sqlType;
        this.sqlTypeName = sqlTypeName;
        this.ordinalPosition = ordinalPosition;
    }
}

final class ForumExplorerDataAccessException extends RuntimeException {
    ForumExplorerDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

final class TableInfo {
    final List<String> columns;
    final List<String> searchableColumns;

    TableInfo(List<String> columns, List<String> searchableColumns) {
        this.columns = columns;
        this.searchableColumns = searchableColumns;
    }
}

