package reaper.android.app.trigger.user;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FacebookFriendsUpdatedOnServerTrigger
{
    private boolean isPolling;

    public FacebookFriendsUpdatedOnServerTrigger(boolean isPolling)
    {
        this.isPolling = isPolling;
    }

    public boolean isPolling()
    {
        return isPolling;
    }
}
