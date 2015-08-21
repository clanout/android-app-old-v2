package reaper.android.app.cache.old.user;

import android.util.Log;

import java.util.List;

import reaper.android.app.model.Friend;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserCache
{
    private static final String TAG = "UserCache";

    private UserCacheDataSource dataSource;

    public UserCache()
    {
        dataSource = UserCacheDataSourceFactory.create();
    }

    public void saveFriends(final List<Friend> friends)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.deleteFriends();
                        dataSource.writeFriends(friends);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Unable to save friends cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public Observable<List<Friend>> getFriends()
    {
        return Observable.create(new Observable.OnSubscribe<List<Friend>>()
        {
            @Override
            public void call(Subscriber<? super List<Friend>> subscriber)
            {
                subscriber.onNext(dataSource.readFriends());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public void evictFriendsCache()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.deleteFriends();
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Unable to evict friends cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void saveContacts(final List<Friend> contacts)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.deleteContacts();
                        dataSource.writeContacts(contacts);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Unable to save contacts cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public Observable<List<Friend>> getContacts()
    {
        return Observable.create(new Observable.OnSubscribe<List<Friend>>()
        {
            @Override
            public void call(Subscriber<? super List<Friend>> subscriber)
            {
                subscriber.onNext(dataSource.readContacts());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public void evictContactsCache()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        dataSource.deleteContacts();
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "Unable to evict contacts cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }
}
