package reaper.android.app.cache.notification;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.DatabaseManager;
import reaper.android.app.cache.core.SQLiteCacheContract;
import reaper.android.common.notification.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteNotificationCache implements NotificationCache
{
    private static final String TAG = "NotificationCache";

    private static SQLiteNotificationCache instance;

    public static SQLiteNotificationCache getInstance()
    {
        if (instance == null)
        {
            instance = new SQLiteNotificationCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;

    private SQLiteNotificationCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
        Timber.d("SQLiteNotificationCache initialized");
    }

    @Override
    public Observable<Object> put(final Notification notification)
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("NotificationCache.put() on thread = " + Thread.currentThread()
                                                                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification.SQL_INSERT);

                            statement.bindLong(1, notification.getId());
                            statement.bindLong(2, notification.getType());
                            statement.bindString(3, notification.getTitle());
                            statement.bindString(4, notification.getMessage());
                            statement.bindString(5, notification.getEventId());
                            statement.bindLong(6, notification.getTimestamp().getMillis());
                            statement.bindLong(7, notification.getTimestampReceived().getMillis());
                            statement.bindString(8, String.valueOf(notification.isNew()));

                            statement.execute();
                            statement.close();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Notification>> getAll()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Notification>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Notification>> subscriber)
                    {

                        Timber.v("NotificationCache.read() on thread = " + Thread.currentThread()
                                                                                 .getName());

                        List<Notification> notifications = new ArrayList<Notification>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Notification.COLUMN_ID,
                                SQLiteCacheContract.Notification.COLUMN_TYPE,
                                SQLiteCacheContract.Notification.COLUMN_TITLE,
                                SQLiteCacheContract.Notification.COLUMN_MESSAGE,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_ID,
                                SQLiteCacheContract.Notification.COLUMN_TIMESTAMP,
                                SQLiteCacheContract.Notification.COLUMN_IS_NEW
                        };

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Notification.TABLE_NAME, projection, null, null, null, null, SQLiteCacheContract.Notification.COLUMN_TIMESTAMP + " DESC");
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast())
                        {
                            try
                            {
                                Notification notification = new Notification.Builder(cursor
                                        .getInt(0))
                                        .type(cursor.getInt(1))
                                        .title(cursor.getString(2))
                                        .message(cursor.getString(3))
                                        .eventId(cursor.getString(4))
                                        .timestamp(new DateTime(cursor.getLong(5)))
                                        .isNew(Boolean.parseBoolean(cursor.getString(6)))
                                        .build();

                                notifications.add(notification);
                            }
                            catch (Exception e)
                            {
                                Timber.v("Unable to process a notification [" + e
                                        .getMessage() + "]");
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(notifications);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> clear()
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("NotificationCache.remove() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification.SQL_DELETE);
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> markRead()
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("NotificationCache.markRead() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification.SQL_MARK_READ);
                            statement.bindString(1, String.valueOf(false));
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }
}
