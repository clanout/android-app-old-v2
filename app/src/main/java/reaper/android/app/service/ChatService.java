package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
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

    public List<ChatMessage> fetchHistory(MultiUserChat chat, String nickName, String userId, long timeout) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException, MUCNotJoinedException
    {
        List<ChatMessage> chatMessageList = new ArrayList<>();

        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(10);

        chat.join(nickName, null, history, timeout);

        Message message = null;
        while ((message = chat.nextMessage()) != null)
        {
            String[] fromUser = message.getFrom().split("/");
            String[] userDetails = fromUser[1].split("_");

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(message.getBody());
            chatMessage.setSenderName(userDetails[0]);
            chatMessage.setSenderId(userDetails[1]);

            if (userId.equals(userDetails[1]))
            {
                chatMessage.setMe(true);
            }
            else
            {
                chatMessage.setMe(false);
            }

            chatMessageList.add(chatMessage);
        }
        return chatMessageList;
    }

    public void postMessage(MultiUserChat chat, String chatMessage, String nickName) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException
    {
        chat.join(nickName);
        chat.sendMessage(chatMessage);
    }

    public MultiUserChat getMultiUserChat(AbstractXMPPConnection connection, String eventId)
    {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat chat = manager.getMultiUserChat(eventId + XMPP_CHATROOM_PREFIX);
        return chat;
    }

}
