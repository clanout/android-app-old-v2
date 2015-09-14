package reaper.android.app.cache.notification;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;

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
