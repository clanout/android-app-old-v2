package reaper.android.app.cache.event;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.cache._core.DatabaseManager;
import reaper.android.app.cache._core.SQLiteCacheContract;
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
        Timber.d("SQLiteEventCache initialized");
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
                .subscribeOn(Schedulers.io());
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

                        Event event = null;
                        if (!cursor.isAfterLast())
                        {
                            String eventJson = cursor.getString(0);
                            boolean isUpdated = Boolean.parseBoolean(cursor.getString(1));

                            event = gson.fromJson(eventJson, Event.class);
                            event.setIsUpdated(isUpdated);
                        }
                        else
                        {
                            Timber.d("Event not present in cache (" + eventId + ")");
                        }

                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(event);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
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
                                SQLiteDatabase db = databaseManager.openConnection();

                                String[] projection = {SQLiteCacheContract.EventDetails.COLUMN_CONTENT};
                                String selection = SQLiteCacheContract.EventDetails.COLUMN_ID + " = ?";
                                String[] selectionArgs = {eventId};

                                Cursor cursor = db
                                        .query(SQLiteCacheContract.EventDetails.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                                cursor.moveToFirst();

                                EventDetails eventDetails = null;
                                if (!cursor.isAfterLast())
                                {
                                    String eventDetailsJson = cursor.getString(0);
                                    eventDetails = gson
                                            .fromJson(eventDetailsJson, EventDetails.class);
                                }
                                else
                                {
                                    Timber.d("EventDetails not present in cache (" + eventId + ")");
                                }

                                cursor.close();
                                databaseManager.closeConnection();

                                subscriber.onNext(eventDetails);
                                subscriber.onCompleted();
                            }
                        }

                )
                .

                        subscribeOn(Schedulers.io()

                        );
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
                            Timber.v("EventCache.reset() on thread = " + Thread.currentThread()
                                                                               .getName());

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
                        Timber.d("Event cache reset. [New Size = " + events.size() + "]");
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
    public void save(final Event event)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.save() on thread = " + Thread.currentThread()
                                                                              .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
                            statement.bindString(1, event.getId());
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
                            statement.bindString(1, event.getId());
                            statement.bindString(2, gson.toJson(event));
                            statement.bindString(3, String.valueOf(false));
                            statement.bindString(4, String.valueOf(false));
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

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
                        Timber.d("Inserted one event [" + event.getId() + "] in cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to insert event_id = " + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void saveDetails(final EventDetails eventDetails)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.saveDetails() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE_ONE);
                            statement.bindString(1, eventDetails.getId());
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            statement = db
                                    .compileStatement(SQLiteCacheContract.EventDetails.SQL_INSERT);
                            statement.bindString(1, eventDetails.getId());
                            statement.bindString(2, gson.toJson(eventDetails));
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

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
                        Timber.d("Inserted details for event_id = " + eventDetails.getId());
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to insert details for event_id = " + eventDetails
                                .getId() + " [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteAll()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.deleteAll() on thread = " + Thread
                                    .currentThread()
                                    .getName());

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
                        Timber.d("Event cache cleared");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete events cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void delete(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.delete() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
                            statement.bindString(1, eventId);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();
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
                        Timber.d("Deleted one event [" + eventId + "] from cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete event_id = " + eventId + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteDetails(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.deleteDetails() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE_ONE);
                            statement.bindString(1, eventId);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();
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
                        Timber.d("Deleted details for event [" + eventId + "] from cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete details event_id = " + eventId + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteCompletely(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.deleteCompletely() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
                            statement.bindString(1, eventId);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            statement = db
                                    .compileStatement(SQLiteCacheContract.EventDetails.SQL_DELETE_ONE);
                            statement.bindString(1, eventId);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

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
                        Timber.d("Completely Deleted one event [" + eventId + "] from cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to completely delete event_id = " + eventId + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void markUpdated(final List<String> eventIds)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.markUpdated() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            if (!eventIds.isEmpty())
                            {
                                SQLiteDatabase db = databaseManager.openConnection();
                                db.beginTransactionNonExclusive();

                                SQLiteStatement statement = db
                                        .compileStatement(SQLiteCacheContract.Event.SQL_MARK_UPDATED);
                                for (String eventId : eventIds)
                                {
                                    statement.bindString(1, String.valueOf(true));
                                    statement.bindString(2, eventId);
                                    statement.execute();
                                    statement.clearBindings();
                                }
                                statement.close();

                                db.setTransactionSuccessful();
                                db.endTransaction();
                                databaseManager.closeConnection();
                            }

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
                        Timber.d("Marked " + eventIds.size() + " events as updated");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to mark events as updated [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void markSeen(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.markUpdated() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_MARK_UPDATED);
                            statement.bindString(1, String.valueOf(false));
                            statement.bindString(2, eventId);
                            statement.execute();
                            statement.close();
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
                        Timber.d("Marked one event [" + eventId + "] as seen");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to mark event [" + eventId + "] as seen [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }
}
