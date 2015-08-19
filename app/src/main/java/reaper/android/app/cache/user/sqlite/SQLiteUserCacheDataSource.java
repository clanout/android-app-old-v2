package reaper.android.app.cache.user.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.core.SQLiteCacheContract;
import reaper.android.app.cache.user.UserCacheDataSource;
import reaper.android.app.model.Friend;

public class SQLiteUserCacheDataSource implements UserCacheDataSource
{
    private static final String TAG = "UserCacheDatSource";

    private DatabaseManager databaseManager;
    private Gson gson;

    public SQLiteUserCacheDataSource()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
    }

    @Override
    public void writeFriends(List<Friend> friends)
    {
        if (!friends.isEmpty())
        {
            SQLiteDatabase db = databaseManager.openConnection();
            SQLiteStatement statement = db
                    .compileStatement(SQLiteCacheContract.FacebookFriends.SQL_INSERT);
            db.beginTransactionNonExclusive();
            for (Friend friend : friends)
            {
                statement.bindString(1, friend.getId());
                statement.bindString(2, gson.toJson(friend));
                statement.execute();
                statement.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            databaseManager.closeConnection();
        }
    }

    @Override
    public List<Friend> readFriends()
    {
        try
        {
            List<Friend> friends = new ArrayList<Friend>();

            SQLiteDatabase db = databaseManager.openConnection();
            String[] projection = {
                    SQLiteCacheContract.FacebookFriends.COLUMN_CONTENT
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.FacebookFriends.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String friendJson = cursor.getString(0);

                Friend friend = gson.fromJson(friendJson, Friend.class);
                friends.add(friend);

                cursor.moveToNext();
            }
            cursor.close();
            databaseManager.closeConnection();

            return friends;
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteFriends()
    {
        SQLiteDatabase db = databaseManager.openConnection();
        SQLiteStatement statement = db
                .compileStatement(SQLiteCacheContract.FacebookFriends.SQL_DELETE);
        statement.execute();
        statement.close();
        databaseManager.closeConnection();
    }

    @Override
    public void writeContacts(List<Friend> contacts)
    {
        if (!contacts.isEmpty())
        {
            SQLiteDatabase db = databaseManager.openConnection();
            SQLiteStatement statement = db
                    .compileStatement(SQLiteCacheContract.PhoneContacts.SQL_INSERT);
            db.beginTransactionNonExclusive();
            for (Friend contact : contacts)
            {
                statement.bindString(1, contact.getId());
                statement.bindString(2, gson.toJson(contact));
                statement.execute();
                statement.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            databaseManager.closeConnection();
        }

    }

    @Override
    public List<Friend> readContacts()
    {
        try
        {
            List<Friend> contacts = new ArrayList<Friend>();

            SQLiteDatabase db = databaseManager.openConnection();
            String[] projection = {
                    SQLiteCacheContract.PhoneContacts.COLUMN_CONTENT
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.PhoneContacts.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String contactJson = cursor.getString(0);

                Friend contact = gson.fromJson(contactJson, Friend.class);
                contacts.add(contact);

                cursor.moveToNext();
            }
            cursor.close();
            databaseManager.closeConnection();

            return contacts;
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteContacts()
    {
        SQLiteDatabase db = databaseManager.openConnection();
        SQLiteStatement statement = db
                .compileStatement(SQLiteCacheContract.PhoneContacts.SQL_DELETE);
        statement.execute();
        statement.close();
        databaseManager.closeConnection();
    }
}
