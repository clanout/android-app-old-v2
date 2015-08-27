package reaper.android.app.trigger.facebook;

import java.util.List;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FacebookFriendsIdFetchedTrigger
{
    private List<String> friendsIdList;
    private boolean isPolling;

    public FacebookFriendsIdFetchedTrigger(List<String> friendsIdList, boolean isPolling)
    {
        this.friendsIdList = friendsIdList;
        this.isPolling = isPolling;
    }

    public boolean isPolling()
    {
        return isPolling;
    }

    public List<String> getFriendsIdList()
    {
        return friendsIdList;
    }
}
