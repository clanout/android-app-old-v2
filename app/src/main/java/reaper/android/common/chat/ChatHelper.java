package reaper.android.common.chat;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ChatHelper
{
    private static AbstractXMPPConnection connection;
    private static final String SERVICE_NAME = "52.1.78.109";
    private static final String HOST = "52.1.78.109";
    private static final int PORT = 5222;

    public static AbstractXMPPConnection getXmppConnection() throws IOException, XMPPException, SmackException
    {
        if (connection == null)
        {
            throw new IllegalStateException("XMPP connection not initialized");
        }
        else
        {
            return connection;
        }
    }

    public static void init(final String userId)
    {
        if (connection == null)
        {
            Observable.just(createConnection(userId))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<AbstractXMPPConnection>()
                    {
                        @Override
                        public void onCompleted()
                        {

                            connection.addConnectionListener(new ConnectionListener()
                            {
                                @Override
                                public void connected(XMPPConnection connection)
                                {
                                }

                                @Override
                                public void authenticated(XMPPConnection connection, boolean resumed)
                                {
                                }

                                @Override
                                public void connectionClosed()
                                {
                                }

                                @Override
                                public void connectionClosedOnError(Exception e)
                                {
                                }

                                @Override
                                public void reconnectionSuccessful()
                                {
                                }

                                @Override
                                public void reconnectingIn(int seconds)
                                {
                                }

                                @Override
                                public void reconnectionFailed(Exception e)
                                {
                                }
                            });

                            connection.addAsyncStanzaListener(new StanzaListener()
                            {
                                @Override
                                public void processPacket(Stanza packet) throws SmackException.NotConnectedException
                                {
                                }
                            }, null);

                            if (connection != null)
                            {
                                try
                                {
                                    connection.connect();
                                    connection.login();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e)
                        {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(AbstractXMPPConnection xmppConnection)
                        {
                            connection = xmppConnection;
                            if (connection == null)
                            {
                                Log.d("APP", "Unable to create xmpp conn");
                            }
                        }
                    });
        }
    }

    private static AbstractXMPPConnection createConnection(final String userId)
    {
        try
        {
            XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(userId, userId)
                    .setServiceName(SERVICE_NAME)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setHost(HOST)
                    .setPort(PORT)
                    .build();

            return new XMPPTCPConnection(configuration);
        }
        catch (Exception e)
        {
            throw null;
        }
    }

    public static void disconnectConnection()
    {
        if (connection != null)
        {
            connection.disconnect();
        }
    }
}
