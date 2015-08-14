package reaper.android.app.cache.core;

public abstract class SQLiteCacheContract
{
    public static abstract class Event
    {
        public static final String TABLE_NAME = "event_cache";

        public static final String COLUMN_ID = "event_id";
        public static final String COLUMN_CONTENT = "json";
        public static final String COLUMN_UPDATES = "is_updated";
        public static final String COLUMN_CHAT_UPDATES = "is_chat_updated";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_UPDATES + " TEXT, " +
                COLUMN_CHAT_UPDATES + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?)";
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_MARK_UPDATED = "UPDATE " + TABLE_NAME + " SET " + COLUMN_UPDATES + " = ? WHERE " + COLUMN_ID + " = ?";
        public static final String SQL_MARK_CHAT_UPDATED = "UPDATE " + TABLE_NAME + " SET " + COLUMN_CHAT_UPDATES + " = ? WHERE " + COLUMN_ID + " = ?";
    }

    public static abstract class EventDetails
    {
        public static final String TABLE_NAME = "event_details_cache";

        public static final String COLUMN_ID = "event_id";
        public static final String COLUMN_CONTENT = "json";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME +" VALUES (?,?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME +" WHERE " + COLUMN_ID + " = ?";
    }
}
