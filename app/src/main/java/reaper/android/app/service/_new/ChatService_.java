package reaper.android.app.service._new;

import org.jivesoftware.smack.AbstractConnectionClosedListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.List;

import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.config.AppConstants;
import reaper.android.app.model.ChatMessage;
import reaper.android.app.service.UserService;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ChatService_
{
    private static ChatService_ instance;

    public static void init(UserService userService)
    {
        instance = new ChatService_(userService);
    }

    public static ChatService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[ChatService Not Initialized]");
        }

        return instance;
    }

    private static final int DEFAULT_HISTORY_SIZE = 20;

    private UserService userService;

    /* Xmpp connection */
    private AbstractXMPPConnection connection;
    private boolean isHealthy;

    /* Clan chat */
    private String activeChat;
    private MultiUserChat chat;

    /* Message Listener */
    private MessageListener messageListener;

    private ChatService_(UserService userService)
    {
        this.userService = userService;

        isHealthy = false;

        activeChat = null;
        chat = null;

        initXmppConnection();
        connect();
    }

    public Observable<Boolean> isHealthy()
    {
        if (isHealthy)
        {
            return Observable.just(true);
        }
        else
        {
            return getConnection()
                    .map(new Func1<AbstractXMPPConnection, Boolean>()
                    {
                        @Override
                        public Boolean call(AbstractXMPPConnection connection)
                        {
                            return isHealthy;
                        }
                    });
        }
    }

    public Observable<ChatMessage> joinChat(final String eventId)
    {
        if (activeChat != null)
        {
            return Observable
                    .error(new IllegalStateException("[Join Chat Failed] Please leave the old chat to join this one"));
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<Message>()
                    {
                        @Override
                        public void call(final Subscriber<? super Message> subscriber)
                        {
                            MultiUserChatManager manager = MultiUserChatManager
                                    .getInstanceFor(connection);
                            chat = manager.getMultiUserChat(eventId + AppConstants.CHAT_POSTFIX);

                            DiscussionHistory history = new DiscussionHistory();
                            history.setMaxStanzas(DEFAULT_HISTORY_SIZE);

                            messageListener = new MessageListener()
                            {
                                @Override
                                public void processMessage(Message message)
                                {
                                    subscriber.onNext(message);
                                }
                            };
                            chat.addMessageListener(messageListener);

                            try
                            {
                                chat.join(getNickname(), null, history, connection
                                        .getPacketReplyTimeout());
                                activeChat = eventId;
                            }
                            catch (Exception e)
                            {
                                subscriber.onError(new Exception("[Unable to join clan chat] " + e
                                        .getMessage()));
                            }
                        }
                    })
                    .map(new Func1<Message, ChatMessage>()
                    {
                        @Override
                        public ChatMessage call(Message message)
                        {
                            return map(message);
                        }
                    })
                    .filter(new Func1<ChatMessage, Boolean>()
                    {
                        @Override
                        public Boolean call(ChatMessage chatMessage)
                        {
                            return chatMessage != null;
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    public Observable<ChatMessage> fetchHistory(final int historySize, final List<ChatMessage> availableMessages)
    {
        if (chat == null)
        {
            return Observable.error(new IllegalStateException("[Chat not joined]"));
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<Message>()
                    {
                        @Override
                        public void call(final Subscriber<? super Message> subscriber)
                        {
                            try
                            {
                                chat.removeMessageListener(messageListener);
                                messageListener = null;
                                chat.leave();

                                DiscussionHistory history = new DiscussionHistory();
                                history.setMaxStanzas(DEFAULT_HISTORY_SIZE * (historySize + 1));

                                messageListener = new MessageListener()
                                {
                                    @Override
                                    public void processMessage(Message message)
                                    {
                                        subscriber.onNext(message);
                                    }
                                };
                                chat.addMessageListener(messageListener);

                                chat.join(getNickname(), null, history, connection
                                        .getPacketReplyTimeout());
                            }
                            catch (Exception e)
                            {
                                subscriber.onError(e);
                            }
                        }
                    })
                    .map(new Func1<Message, ChatMessage>()
                    {
                        @Override
                        public ChatMessage call(Message message)
                        {
                            return map(message);
                        }
                    })
                    .filter(new Func1<ChatMessage, Boolean>()
                    {
                        @Override
                        public Boolean call(ChatMessage chatMessage)
                        {
                            return chatMessage != null && !availableMessages.contains(chatMessage);
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    public Observable<Object> post(ChatMessage message)
    {
        if (activeChat == null || chat == null)
        {
            Timber.e("[No active chat]");
            return Observable.error(new Exception("No active chat"));
        }

        try
        {
            chat.sendMessage(map(message));
            return Observable.empty();
        }
        catch (SmackException.NotConnectedException e)
        {
            return Observable.error(e);
        }
    }

    public void leaveChat()
    {
        if (chat != null)
        {
            try
            {
                chat.removeMessageListener(messageListener);
                chat.leave();
                messageListener = null;
                activeChat = null;
            }
            catch (SmackException.NotConnectedException e)
            {
                Timber.e("[Leave Chat Failed] " + e.getMessage());
            }
        }
    }

    /* Helper Methods */
    private void initXmppConnection()
    {
        try
        {
            String userId = userService.getSessionUserId();

            XMPPTCPConnectionConfiguration configuration =
                    XMPPTCPConnectionConfiguration
                            .builder()
                            .setUsernameAndPassword(userId, userId)
                            .setServiceName(AppConstants.CHAT_SERVICE_NAME)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setHost(AppConstants.CHAT_SERVICE_HOST)
                            .setPort(AppConstants.CHAT_SERVICE_PORT)
                            .build();

            connection = new XMPPTCPConnection(configuration);

            connection.addConnectionListener(new AbstractConnectionClosedListener()
            {
                @Override
                public void connectionTerminated()
                {
                    Timber.v("[XmppConnection Terminated]");
                    isHealthy = false;
                }
            });
        }
        catch (Exception e)
        {
            Timber.v("[XmppConnection Creation Failed] " + e.getMessage());
        }
    }

    private Observable<AbstractXMPPConnection> getConnection()
    {
        if (isHealthy)
        {
            return Observable.just(connection);
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<AbstractXMPPConnection>()
                    {
                        @Override
                        public void call(Subscriber<? super AbstractXMPPConnection> subscriber)
                        {
                            try
                            {
                                if (!connection.isConnected())
                                {
                                    connection.connect();
                                }

                                if (!connection.isAnonymous())
                                {
                                    connection.login();
                                }

                                subscriber.onNext(connection);
                                subscriber.onCompleted();
                            }
                            catch (Exception e)
                            {
                                subscriber.onError(e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io());
        }
    }

    private void connect()
    {
        Timber.v("[XmppConnection] Establishing connection... ");
        getConnection()
                .subscribe(new Subscriber<AbstractXMPPConnection>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.v("[XmppConnection Established]");
                        isHealthy = true;
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.v("[XmppConnection Connection Failed] " + e.getMessage());
                    }

                    @Override
                    public void onNext(AbstractXMPPConnection connection)
                    {
                    }
                });
    }

    private ChatMessage map(Message message)
    {
        try
        {
            return GsonProvider.getGson()
                               .fromJson(message.getBody(), ChatMessage.class);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Message map(ChatMessage message)
    {
        Message msg = new Message();
        msg.setStanzaId(message.getId());
        msg.setBody(GsonProvider.getGson().toJson(message));
        return msg;
    }

    private String getNickname()
    {
        return userService.getSessionUserId();
    }
}
