package reaper.android.common.chat;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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
        Log.d("APP", "xmpp connection ----- " + connection);
        if (connection == null)
        {
            Log.d("APP", "xmpp connection is null");
            Observable.just(createConnection(userId))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<AbstractXMPPConnection>()
                    {
                        @Override
                        public void onCompleted()
                        {
                            Log.d("APP", "xmpp connection in on completed----- " + connection);
                            connection.addConnectionListener(new ConnectionListener()
                            {
                                @Override
                                public void connected(XMPPConnection connection)
                                {
                                    Log.d("APP", "xmpp connection in on connected ----- " + connection);
                                }

                                @Override
                                public void authenticated(XMPPConnection connection, boolean resumed)
                                {
                                    Log.d("APP", "xmpp connection in on authenticated----- " + connection);
                                }

                                @Override
                                public void connectionClosed()
                                {
                                    Log.d("APP", "xmpp connection closed");
                                }

                                @Override
                                public void connectionClosedOnError(Exception e)
                                {
                                    Log.d("APP", "xmpp connection closed on error----- " + e.getMessage());
                                }

                                @Override
                                public void reconnectionSuccessful()
                                {
                                    Log.d("APP", "xmpp connection reconnectioin successful");
                                }

                                @Override
                                public void reconnectingIn(int seconds)
                                {
                                    Log.d("APP", "xmpp connection reconnecting in " + seconds);
                                }

                                @Override
                                public void reconnectionFailed(Exception e)
                                {
                                    Log.d("APP", "xmpp connection reconnection failed");
                                }
                            });


                            if (connection != null)
                            {
                                Log.d("APP", "xmpp connection is not null");
                                try
                                {
                                    Log.d("APP", "trying login");
                                    connection.connect();
                                    connection.login();
                                } catch (Exception e)
                                {
                                    Log.d("APP", "xmpp connection exception ----- " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e)
                        {
                            e.printStackTrace();
                            Log.d("APP", "xmpp onerror ----- " + e.getMessage());
                        }

                        @Override
                        public void onNext(AbstractXMPPConnection xmppConnection)
                        {
                            Log.d("APP", "xmpp connection in on next----- " + xmppConnection);
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
            Log.d("APP", "trying to create connection");
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
            Log.d("APP", "xmpp connection exception while configuration----- " + e.getMessage());
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
