package reaper.android.app.service;

import com.squareup.otto.Bus;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
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

    public static List<ChatMessage> fetchHistory(String eventId, AbstractXMPPConnection connection, String nickName) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException, MUCNotJoinedException
    {
        List<ChatMessage> chatMessageList = new ArrayList<>();

        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat chat = manager.getMultiUserChat(eventId + XMPP_CHATROOM_PREFIX);

        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(50);

        chat.join(nickName, null, history, connection.getPacketReplyTimeout());

        Message message = null;
        while ((message = chat.nextMessage()) != null){

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(message.getBody());
            chatMessage.setSenderName(message.getFrom());
            chatMessage.setSenderId(message.getFrom());

            chatMessageList.add(chatMessage);
        }

        return chatMessageList;
    }


}
