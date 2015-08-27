package reaper.android.app.trigger.user;

/**
 * Created by Aditya on 27-08-2015.
 */
public class NewSessionCreatedTrigger
{
    private String sessionCookie;

    public NewSessionCreatedTrigger(String sessionCookie)
    {
        this.sessionCookie = sessionCookie;
    }

    public String getSessionCookie()
    {
        return sessionCookie;
    }
}
