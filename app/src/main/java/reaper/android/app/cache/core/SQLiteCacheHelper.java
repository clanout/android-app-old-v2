package reaper.android.app.cache.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SQLiteCacheHelper extends SQLiteOpenHelper
{
    private static final String TAG = "SQLiteCacheHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reaper.db";

    public SQLiteCacheHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQLiteCacheContract.Event.SQL_CREATE_TABLE);
        db.execSQL(SQLiteCacheContract.EventDetails.SQL_CREATE_TABLE);
        db.execSQL(SQLiteCacheContract.Generic.SQL_CREATE_TABLE);
        db.execSQL(SQLiteCacheContract.FacebookFriends.SQL_CREATE_TABLE);
        db.execSQL(SQLiteCacheContract.PhoneContacts.SQL_CREATE_TABLE);
        Log.d(TAG, "Cache database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQLiteCacheContract.Event.SQL_DELETE_TABLE);
        db.execSQL(SQLiteCacheContract.EventDetails.SQL_DELETE_TABLE);
        db.execSQL(SQLiteCacheContract.Generic.SQL_DELETE_TABLE);
        db.execSQL(SQLiteCacheContract.FacebookFriends.SQL_DELETE_TABLE);
        db.execSQL(SQLiteCacheContract.PhoneContacts.SQL_DELETE_TABLE);
        onCreate(db);
        Log.d(TAG, "Cache database upgraded");
    }
}
