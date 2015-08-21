package reaper.android.app.cache.old.user;

import java.util.List;

import reaper.android.app.model.Friend;

public interface UserCacheDataSource
{
    void writeFriends(List<Friend> friends);

    List<Friend> readFriends();

    void deleteFriends();

    void writeContacts(List<Friend> contacts);

    List<Friend> readContacts();

    void deleteContacts();
}
