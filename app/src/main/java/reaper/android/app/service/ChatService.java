package reaper.android.app.service;

import com.squareup.otto.Bus;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MUCNotJoinedException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.ChatMessage;

public class ChatService
{
    private Bus bus;
    private UserService userService;
    private static final String XMPP_CHATROOM_PREFIX = "@conference.52.1.78.109";

    public ChatService(Bus bus)
    {
        this.bus = bus;
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

}
