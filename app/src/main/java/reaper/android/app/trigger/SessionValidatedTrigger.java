package reaper.android.app.trigger;

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
