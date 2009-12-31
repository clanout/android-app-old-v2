package reaper.android.app.trigger.user;

public class SessionValidatedTrigger
{
    private String sessionCookie;

    public SessionValidatedTrigger(String sessionCookie)
    {
        this.sessionCookie = sessionCookie;
    }

    public String getValidatedSessionCookie()
    {
        return sessionCookie;
    }
}
