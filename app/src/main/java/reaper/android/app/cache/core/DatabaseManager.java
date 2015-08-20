package reaper.android.app.cache.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager
{
    private static final String TAG = "DatabaseManager";

    private static SQLiteCacheHelper sqliteCacheHelper;
    private static DatabaseManager instance;

    private SQLiteDatabase database;
    private AtomicInteger connectionCounter;

    private DatabaseManager()
    {
        connectionCounter = new AtomicInteger(0);
    }

    public static synchronized void init(Context context)
    {
        instance = new DatabaseManager();
        sqliteCacheHelper = new SQLiteCacheHelper(context);
        Log.d(TAG, "DatabaseManager initialized");
    }

    public static synchronized DatabaseManager getInstance()
    {
        if(instance == null)
        {
            Log.e(TAG, "DatabaseManager not initialized");
            throw new IllegalStateException("DatabaseManager not initialized");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openConnection()
    {
        if(connectionCounter.incrementAndGet() == 1)
        {
            database = sqliteCacheHelper.getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeConnection()
    {
        if(connectionCounter.decrementAndGet() == 0)
        {
            database.close();
            database = null;
        }
    }
}
