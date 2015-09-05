package reaper.android.app.trigger.chat;

import org.jivesoftware.smack.AbstractXMPPConnection;

/**
 * Created by Aditya on 05-09-2015.
 */
public class GotXmppConnectionTrigger
{
    private AbstractXMPPConnection connection;

    public GotXmppConnectionTrigger(AbstractXMPPConnection connection)
    {
        this.connection = connection;
    }

    public AbstractXMPPConnection getConnection()
    {
        return connection;
    }
}
