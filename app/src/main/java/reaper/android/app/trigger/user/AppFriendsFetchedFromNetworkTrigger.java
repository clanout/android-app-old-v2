package reaper.android.app.trigger.user;

import java.util.List;

import reaper.android.app.model.Friend;

/**
 * Created by Aditya on 27-08-2015.
 */
public class AppFriendsFetchedFromNetworkTrigger
{
    private List<Friend> friends;

    public AppFriendsFetchedFromNetworkTrigger(List<Friend> friends)
    {
        this.friends = friends;
    }

    public List<Friend> getFriends()
    {
        return friends;
    }
}
