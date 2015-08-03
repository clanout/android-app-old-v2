package reaper.android.app.ui.screens.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.jivesoftware.smack.AbstractXMPPConnection;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.ChatMessage;
import reaper.android.app.service.ChatService;
import reaper.android.app.service.UserService;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;


/**
 * Created by harsh on 21-05-2015.
 */
public class ChatFragment extends Fragment implements View.OnClickListener
{

    private EditText typeMessage;
    private ImageButton send;
    private ListView listView;
    private TextView noSessionMessage;
    private RelativeLayout mainContent;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private String eventId;

    private AbstractXMPPConnection connection;
    private UserService userService;
    private Bus bus;
    private ChatService chatService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        typeMessage = (EditText) view.findViewById(R.id.etTypeMessageChat);
        send = (ImageButton) view.findViewById(R.id.ib_fragment_chat_send);
        listView = (ListView) view.findViewById(R.id.lvChat);
        noSessionMessage = (TextView) view.findViewById(R.id.tv_fragment_chat_no_session);
        mainContent = (RelativeLayout) view.findViewById(R.id.rl_fragment_chat_main_content);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        send.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            renderNoSessionView();
        }

        eventId = (String) bundle.get("event_id");
        if (eventId == null || eventId.isEmpty())
        {
            renderNoSessionView();
        }

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        chatService = new ChatService(bus);

        try
        {
            connection = ChatHelper.getXmppConnection();
        }
        catch (Exception e)
        {
            renderNoSessionView();
        }

        if (connection == null)
        {
            renderNoSessionView();
        }
        else
        {
            renderChatView();
            chatMessageList = new ArrayList<>();

            initChatAdapter();

            renderHistory();
        }
    }

    private void renderNoSessionView()
    {
        mainContent.setVisibility(View.GONE);
        noSessionMessage.setVisibility(View.VISIBLE);
        noSessionMessage.setText("Cannot fetch the messages at the moment. Please try again.");
        return;
    }

    private void renderChatView()
    {
        mainContent.setVisibility(View.VISIBLE);
        noSessionMessage.setVisibility(View.GONE);
    }

    private void initChatAdapter()
    {
        chatAdapter = new ChatAdapter(new ArrayList<ChatMessage>(), getActivity());
        listView.setAdapter(chatAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    public void displayMessage(ChatMessage message)
    {
        chatAdapter.add(message);
        chatAdapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll()
    {
        listView.setSelection(listView.getCount() - 1);
    }

    private void renderHistory()
    {
        try
        {
            chatMessageList = chatService.fetchHistory(eventId, connection, userService.getActiveUserName());
        }
        catch (Exception e)
        {
            renderNoSessionView();
        }

        if (chatMessageList == null)
        {
            renderNoSessionView();
        }
        else
        {
            for (int i = 0; i < chatMessageList.size(); i++)
            {
                ChatMessage message = chatMessageList.get(i);
                displayMessage(message);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.ib_fragment_chat_send)
        {

            String message = typeMessage.getText().toString();
            if (TextUtils.isEmpty(message))
            {
                return;
            }

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(message);
            chatMessage.setSenderName(userService.getActiveUserName());
            chatMessage.setMe(true);
            chatMessage.setSenderId(userService.getActiveUserId());

            typeMessage.setText("");
            displayMessage(chatMessage);

            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        }
    }
}
