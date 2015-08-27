package reaper.android.app.trigger.user;

import java.util.List;

import reaper.android.app.model.Friend;

public class AllAppFriendsFetchedTrigger
{
    private List<Friend> friends;

    public AllAppFriendsFetchedTrigger(List<Friend> friends)
    {
        this.friends = friends;
    }

    public List<Friend> getFriends()
    {
        return friends;
    }
}
