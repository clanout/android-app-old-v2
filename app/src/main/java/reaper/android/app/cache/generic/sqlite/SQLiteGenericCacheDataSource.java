package reaper.android.app.cache.generic.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.core.SQLiteCacheContract;
import reaper.android.app.cache.generic.GenericCacheDataSource;

public class SQLiteGenericCacheDataSource implements GenericCacheDataSource
{
    private static final String TAG = "GenericCacheDataSource";

    private DatabaseManager databaseManager;

    public SQLiteGenericCacheDataSource()
    {
        databaseManager = DatabaseManager.getInstance();
    }

    @Override
    public void write(String key, String value)
    {
        SQLiteDatabase db = databaseManager.openConnection();
        db.beginTransactionNonExclusive();

        SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Generic.SQL_DELETE);
        statement.bindString(1, key);
        statement.execute();
        statement.clearBindings();

        statement = db.compileStatement(SQLiteCacheContract.Generic.SQL_INSERT);
        statement.bindString(1, key);
        statement.bindString(2, value);
        statement.execute();
        statement.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();

        statement.close();
        databaseManager.closeConnection();

        Log.d(TAG, "Cache (Insert) : " + key + " = " + value);
    }

    @Override
    public String read(String key)
    {
        try
        {
            SQLiteDatabase db = databaseManager.openConnection();
            String[] projection = {
                    SQLiteCacheContract.Generic.COLUMN_KEY
            };

            String selection = SQLiteCacheContract.Generic.COLUMN_KEY + " = ?";

            String[] selectionArgs = {
                    key
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.Generic.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            cursor.moveToFirst();

            String value = cursor.getString(0);

            cursor.close();
            databaseManager.closeConnection();

            return value;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void delete(String key)
    {
        SQLiteDatabase db = databaseManager.openConnection();

        SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Generic.SQL_DELETE);
        statement.bindString(1, key);
        statement.execute();
        statement.close();
        databaseManager.closeConnection();

        Log.d(TAG, "Cache (Delete) : " + key);
    }
}
