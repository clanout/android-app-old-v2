package reaper.android.app.cache.event.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.SQLiteCacheContract;
import reaper.android.app.cache.core.SQLiteCacheHelper;
import reaper.android.app.cache.event.EventCacheDataSource;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;

public class SQLiteEventCacheDataSource implements EventCacheDataSource
{
    private static final String TAG = "EventCacheDatSource";

    private SQLiteCacheHelper sqliteCacheHelper;
    private Gson gson;

    public SQLiteEventCacheDataSource()
    {
        sqliteCacheHelper = SQLiteCacheHelper.getInstance();
        gson = GsonProvider.getGson();
    }

    @Override
    public void write(List<Event> events)
    {
        if (!events.isEmpty())
        {
            SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
            db.beginTransactionNonExclusive();
            for (Event event : events)
            {
                statement.bindString(1, event.getId());
                statement.bindString(2, gson.toJson(event));
                statement.bindString(3, String.valueOf(false));
                statement.bindString(4, String.valueOf(false));
                statement.execute();
                statement.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            db.close();
        }

        Log.d(TAG, "Inserted " + events.size() + " events into cache db");
    }

    @Override
    public void write(Event event)
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();

        SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
        statement.bindString(1, event.getId());
        statement.execute();
        statement.clearBindings();

        statement = db.compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
        statement.bindString(1, event.getId());
        statement.bindString(2, gson.toJson(event));
        statement.bindString(3, String.valueOf(false));
        statement.bindString(4, String.valueOf(false));
        statement.execute();
        statement.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();

        statement.close();
        db.close();

        Log.d(TAG, "Inserted event = " + event.getId() + " into cache db");
    }

    @Override
    public List<Event> read()
    {
        try
        {
            List<Event> events = new ArrayList<Event>();

            SQLiteDatabase db = sqliteCacheHelper.getReadableDatabase();
            String[] projection = {
                    SQLiteCacheContract.Event.COLUMN_CONTENT,
                    SQLiteCacheContract.Event.COLUMN_UPDATES,
                    SQLiteCacheContract.Event.COLUMN_CHAT_UPDATES
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.Event.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String eventJson = cursor.getString(0);
                boolean isUpdated = Boolean.parseBoolean(cursor.getString(1));
                boolean isChatUpdated = Boolean.parseBoolean(cursor.getString(2));

                Event event = gson.fromJson(eventJson, Event.class);
                event.setIsUpdated(isUpdated);
                event.setIsChatUpdated(isChatUpdated);
                events.add(event);

                cursor.moveToNext();
            }
            cursor.close();
            db.close();

            return events;
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    @Override
    public Event read(final String eventId)
    {
        try
        {
            SQLiteDatabase db = sqliteCacheHelper.getReadableDatabase();
            String[] projection = {
                    SQLiteCacheContract.Event.COLUMN_CONTENT,
                    SQLiteCacheContract.Event.COLUMN_UPDATES,
                    SQLiteCacheContract.Event.COLUMN_CHAT_UPDATES
            };

            String selection = SQLiteCacheContract.Event.COLUMN_ID + " = ?";

            String[] selectionArgs = {
                    eventId
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.Event.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            cursor.moveToFirst();

            String eventJson = cursor.getString(0);
            boolean isUpdated = Boolean.parseBoolean(cursor.getString(1));
            boolean isChatUpdated = Boolean.parseBoolean(cursor.getString(2));

            Event event = gson.fromJson(eventJson, Event.class);
            event.setIsUpdated(isUpdated);
            event.setIsChatUpdated(isChatUpdated);

            cursor.close();
            db.close();

            return event;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void writeDetails(EventDetails eventDetails)
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();

        SQLiteStatement statement = db
                .compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE_ONE);
        statement.bindString(1, eventDetails.getId());
        statement.execute();
        statement.clearBindings();

        statement = db.compileStatement(SQLiteCacheContract.EventDetails.SQL_INSERT);
        statement.bindString(1, eventDetails.getId());
        statement.bindString(2, gson.toJson(eventDetails));
        statement.execute();
        statement.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();


        statement.close();
        db.close();

        Log.d(TAG, "Inserted details for event = " + eventDetails.getId() + " into cache db");
    }

    @Override
    public EventDetails readDetails(final String eventId)
    {
        try
        {
            SQLiteDatabase db = sqliteCacheHelper.getReadableDatabase();
            String[] projection = {
                    SQLiteCacheContract.EventDetails.COLUMN_CONTENT
            };

            String selection = SQLiteCacheContract.EventDetails.COLUMN_ID + " = ?";

            String[] selectionArgs = {
                    eventId
            };

            Cursor cursor = db
                    .query(SQLiteCacheContract.EventDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            cursor.moveToFirst();

            String eventDetailsJson = cursor.getString(0);

            EventDetails eventDetails = gson.fromJson(eventDetailsJson, EventDetails.class);

            cursor.close();
            db.close();

            return eventDetails;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void delete()
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();

        SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Event.SQL_DELETE);
        statement.execute();
        statement.clearBindings();

        statement = db.compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE);
        statement.execute();
        statement.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();

        statement.close();
        db.close();

        Log.d(TAG, "Deleted all events from cache db");
    }

    @Override
    public void delete(final String eventId, final boolean deleteDetails)
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();

        SQLiteStatement statement = db.compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
        statement.bindString(1, eventId);
        statement.execute();
        statement.clearBindings();

        if(deleteDetails)
        {
            statement = db.compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE_ONE);
            statement.bindString(1, eventId);
            statement.execute();
            statement.clearBindings();
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        statement.close();
        db.close();

        Log.d(TAG, "Deleted event(" + eventId + " from cache db");
    }

    @Override
    public void markUpdated(List<Event> events)
    {
        if (!events.isEmpty())
        {
            SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
            SQLiteStatement statement = db
                    .compileStatement(SQLiteCacheContract.Event.SQL_MARK_UPDATED);
            db.beginTransactionNonExclusive();
            for (Event event : events)
            {
                statement.bindString(1, String.valueOf(true));
                statement.bindString(2, event.getId());
                statement.execute();
                statement.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            db.close();
        }

        Log.d(TAG, "Marked " + events.size() + " events as updated in cache db");
    }

    @Override
    public void setUpdatedFalse(Event event)
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        SQLiteStatement statement = db
                .compileStatement(SQLiteCacheContract.Event.SQL_MARK_UPDATED);
        statement.bindString(1, String.valueOf(false));
        statement.bindString(2, event.getId());
        statement.execute();
        statement.close();
        db.close();
    }

    @Override
    public void markChatUpdated(List<String> eventIds)
    {
        if (!eventIds.isEmpty())
        {
            SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
            SQLiteStatement statement = db
                    .compileStatement(SQLiteCacheContract.Event.SQL_MARK_CHAT_UPDATED);
            db.beginTransactionNonExclusive();
            for (String eventId : eventIds)
            {
                statement.bindString(1, String.valueOf(true));
                statement.bindString(2, eventId);
                statement.execute();
                statement.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            db.close();
        }

        Log.d(TAG, "Marked " + eventIds.size() + " events as chat updated in cache db");
    }

    @Override
    public void setChatUpdatedFalse(final String eventId)
    {
        SQLiteDatabase db = sqliteCacheHelper.getWritableDatabase();
        SQLiteStatement statement = db
                .compileStatement(SQLiteCacheContract.Event.SQL_MARK_CHAT_UPDATED);
        statement.bindString(1, String.valueOf(false));
        statement.bindString(2, eventId);
        statement.execute();

        statement.close();
        db.close();
    }
}
