package reaper.android.app.cache.user;

import java.util.List;

import reaper.android.app.model.Friend;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UserCache
{
    private static final String TAG = "UserCache";

    private UserCacheDataSource dataSource;

    public UserCache()
    {
        dataSource = UserCacheDataSourceFactory.create();
    }

    public void saveFriends(List<Friend> friends)
    {
        evictFriendsCache();
        dataSource.writeFriends(friends);
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
        }).subscribeOn(Schedulers.newThread());
    }

    public void evictFriendsCache()
    {
        dataSource.deleteFriends();
    }

    public void saveContacts(List<Friend> contacts)
    {
        evictContactsCache();
        dataSource.writeContacts(contacts);
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
        }).subscribeOn(Schedulers.newThread());
    }

    public void evictContactsCache()
    {
        dataSource.deleteContacts();
    }
}
