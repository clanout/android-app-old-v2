package reaper.android.app.cache.user;

import java.util.List;

import reaper.android.app.model.Friend;
import rx.Observable;

public interface UserCache
{
    Observable<List<Friend>> getFriends();

    Observable<List<Friend>> getContacts();

    void saveFriends(List<Friend> friends);

    void saveContacts(List<Friend> contacts);

    void deleteFriends();

    void deleteContacts();
}
