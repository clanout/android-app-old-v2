package reaper.android.app.service;

import com.squareup.otto.Bus;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MUCNotJoinedException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.SendChatNotificationApiRequest;
import retrofit.client.Response;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ChatService
{
    private Bus bus;
    private static final String XMPP_CHATROOM_PREFIX = "@conference.52.1.78.109";
    private EventApi eventApi;

    public ChatService(Bus bus)
    {
        this.bus = bus;
        this.eventApi = ApiManager.getInstance().getApi(EventApi.class);
    }

    public void fetchHistory(MultiUserChat chat, String nickName, String userId, long timeout, int maxStanzas) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException, MUCNotJoinedException
    {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(maxStanzas);

        chat.join(nickName, null, history, timeout);
    }

    public void postMessage(MultiUserChat chat, String chatMessage, String nickName, long timeout) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException
    {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        chat.join(nickName, null, history, timeout);
        chat.sendMessage(chatMessage);
    }

    public MultiUserChat getMultiUserChat(AbstractXMPPConnection connection, String eventId)
    {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat chat = manager.getMultiUserChat(eventId + XMPP_CHATROOM_PREFIX);
        return chat;
    }

    public void sendChatNotification(String eventId, String eventName)
    {
        eventApi.sendChatNotification(new SendChatNotificationApiRequest(eventId, eventName)).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Response>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Response response) {

            }
        });
    }

}
