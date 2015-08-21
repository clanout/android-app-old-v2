package reaper.android.app.cache.event;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.core.SQLiteCacheContract;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteEventCache implements EventCache
{
    private static final String TAG = "EventCache";

    private static SQLiteEventCache instance;

    public static SQLiteEventCache getInstance()
    {
        if (instance == null)
        {
            instance = new SQLiteEventCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;

    private SQLiteEventCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
    }

    @Override
    public Observable<List<Event>> getEvents()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Event>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Event>> subscriber)
                    {
                        List<Event> events = new ArrayList<Event>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Event.COLUMN_CONTENT,
                                SQLiteCacheContract.Event.COLUMN_UPDATES
                        };

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Event.TABLE_NAME, projection, null, null, null, null, null);
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast())
                        {
                            String eventJson = cursor.getString(0);
                            boolean isUpdated = Boolean.parseBoolean(cursor.getString(1));

                            Event event = gson.fromJson(eventJson, Event.class);
                            event.setIsUpdated(isUpdated);
                            events.add(event);

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(events);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(Schedulers.io());
    }

    @Override
    public Observable<Event> getEvent(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<Event>()
                {
                    @Override
                    public void call(Subscriber<? super Event> subscriber)
                    {
                        Event event;
                        try
                        {
                            SQLiteDatabase db = databaseManager.openConnection();

                            String[] projection = {
                                    SQLiteCacheContract.Event.COLUMN_CONTENT,
                                    SQLiteCacheContract.Event.COLUMN_UPDATES
                            };
                            String selection = SQLiteCacheContract.Event.COLUMN_ID + " = ?";
                            String[] selectionArgs = {eventId};

                            Cursor cursor = db
                                    .query(SQLiteCacheContract.Event.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                            cursor.moveToFirst();

                            String eventJson = cursor.getString(0);
                            boolean isUpdated = Boolean.parseBoolean(cursor.getString(1));

                            event = gson.fromJson(eventJson, Event.class);
                            event.setIsUpdated(isUpdated);

                            cursor.close();
                            databaseManager.closeConnection();
                        }
                        catch (Exception e)
                        {
                            Timber.e("Unable to read event with event_id = " + eventId + " [" + e
                                    .getMessage() + "]");
                            event = null;
                        }

                        subscriber.onNext(event);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(Schedulers.io());
    }

    @Override
    public Observable<EventDetails> getEventDetails(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<EventDetails>()
                {
                    @Override
                    public void call(Subscriber<? super EventDetails> subscriber)
                    {
                        EventDetails eventDetails;
                        try
                        {
                            SQLiteDatabase db = databaseManager.openConnection();

                            String[] projection = {SQLiteCacheContract.EventDetails.COLUMN_CONTENT};
                            String selection = SQLiteCacheContract.EventDetails.COLUMN_ID + " = ?";
                            String[] selectionArgs = {eventId};

                            Cursor cursor = db
                                    .query(SQLiteCacheContract.EventDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                            cursor.moveToFirst();

                            String eventDetailsJson = cursor.getString(0);
                            eventDetails = gson.fromJson(eventDetailsJson, EventDetails.class);

                            cursor.close();
                            databaseManager.closeConnection();
                        }
                        catch (Exception e)
                        {
                            Timber.e("Unable to read EventDetails for event with event_id = " + eventId + " [" + e
                                    .getMessage() + "]");
                            eventDetails = null;
                        }

                        subscriber.onNext(eventDetails);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(Schedulers.io());
    }

    @Override
    public void reset(final List<Event> events)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            statement = db
                                    .compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            if (!events.isEmpty())
                            {
                                statement = db
                                        .compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
                                for (Event event : events)
                                {
                                    statement.bindString(1, event.getId());
                                    statement.bindString(2, gson.toJson(event));
                                    statement.bindString(3, String.valueOf(false));
                                    statement.bindString(4, String.valueOf(false));
                                    statement.execute();
                                    statement.clearBindings();
                                }
                                statement.close();
                            }

                            db.setTransactionSuccessful();
                            db.endTransaction();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("[Reset] Events cache size = " + events.size());
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to reset events cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void save(Event event)
    {

    }

    @Override
    public void save(EventDetails eventDetails)
    {

    }

    @Override
    public void evict()
    {

    }

    @Override
    public void invalidate(String eventId)
    {

    }

    @Override
    public void invalidateCompletely(String eventId)
    {

    }

    @Override
    public void markUpdated(List<String> eventIds)
    {

    }

    @Override
    public void markSeen(String eventId)
    {

    }
}
