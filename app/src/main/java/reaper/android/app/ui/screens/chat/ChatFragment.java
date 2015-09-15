package reaper.android.app.ui.screens.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.ChatMessage;
import reaper.android.app.model.Event;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.ChatService;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.chat.GotXmppConnectionTrigger;
import reaper.android.app.trigger.chat.XmppConnectionAuthenticatedTrigger;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
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
    private LinearLayout mainContent;
    private Button loadHistory;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private String eventId;

    private AbstractXMPPConnection connection;
    private MultiUserChat multiUserChat;
    private UserService userService;
    private ChatService chatService;
    private EventService eventService;
    private LocationService locationService;
    private FragmentManager fragmentManager;
    private Bus bus;

    private GenericCache genericCache;

    private int loadHistoryClickCount = 1;

    private MessageListener messageListener;
    private Drawable sendDrawable;

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

        typeMessage = (EditText) view.findViewById(R.id.et_chat_fragment_type_message_chat);
        send = (ImageButton) view.findViewById(R.id.ib_fragment_chat_send);
        listView = (ListView) view.findViewById(R.id.lv_chat_fragment);
        noSessionMessage = (TextView) view.findViewById(R.id.tv_fragment_chat_no_session);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_chat_main_content);
        loadHistory = (Button) view.findViewById(R.id.b_chat_fragment_load_history);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        send.setOnClickListener(this);
        loadHistory.setOnClickListener(this);

        generateDrawables();
        send.setImageDrawable(sendDrawable);

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            renderNoSessionView();
        }

        eventId = (String) bundle.get(BundleKeys.CHAT_FRAGMENT_EVENT_ID);
        if (eventId == null || eventId.isEmpty())
        {
            renderNoSessionView();
        }

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        chatService = new ChatService(bus);
        eventService =
                new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getActivity().getSupportFragmentManager();

        genericCache = CacheManager.getGenericCache();
    }

    private void generateDrawables()
    {
        sendDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.SEND)
                .setColor(getResources().getColor(R.color.accent))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.CHAT_FRAGMENT);

        bus.register(this);

        try
        {
            ChatHelper.getXmppConnection();
        } catch (Exception e)
        {
            renderNoSessionView();
        }

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.CHAT);
    }

    private void renderNoSessionView()
    {
        mainContent.setVisibility(View.GONE);
        noSessionMessage.setVisibility(View.VISIBLE);
        noSessionMessage.setText(R.string.chat_messages_not_fetched);
        return;
    }

    private void renderChatView()
    {
        mainContent.setVisibility(View.VISIBLE);
        loadHistory.setVisibility(View.GONE);
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
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (messageListener != null)
        {
            multiUserChat.removeMessageListener(messageListener);
            messageListener = null;
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        bus.unregister(this);
    }

    public void displayMessage(final ChatMessage message)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (chatMessageList.size() >= 20 * loadHistoryClickCount)
                {
                    loadHistory.setVisibility(View.VISIBLE);
                } else
                {
                    loadHistory.setVisibility(View.GONE);
                }
                chatAdapter.add(message);
                chatAdapter.notifyDataSetChanged();
                scroll();
            }
        });
    }

    private void scroll()
    {
        listView.setSelection(listView.getCount() - 1);
    }

    private void renderHistory()
    {
        try
        {
            chatService.fetchHistory(multiUserChat, userService.getActiveUserName() + "_" + userService.getActiveUserId(), userService.getActiveUserId(), connection.getPacketReplyTimeout(), 20);
        } catch (Exception e)
        {
            renderNoSessionView();
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

            try
            {
                chatService.postMessage(multiUserChat, message, userService.getActiveUserName() + "_" + userService.getActiveUserId(), connection.getPacketReplyTimeout());
            } catch (Exception e)
            {
                typeMessage.setText(message);
                Toast.makeText(getActivity(), R.string.chat_message_not_sent, Toast.LENGTH_LONG).show();
                return;
            }

            typeMessage.setText("");

            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        } else if (v.getId() == R.id.b_chat_fragment_load_history)
        {
            loadHistory.setClickable(false);
            loadHistoryClickCount++;

            chatMessageList = new ArrayList<>();
            chatAdapter.clear();

            int maxStanzas = 20 * loadHistoryClickCount;

            try
            {
                chatService.fetchHistory(multiUserChat, userService.getActiveUserName() + "_" + userService.getActiveUserId(), userService.getActiveUserId(), connection.getPacketReplyTimeout(), maxStanzas);
            } catch (Exception e)
            {
                renderNoSessionView();
            }
            loadHistory.setClickable(true);
        }
    }

    @Subscribe
    public void xmppConnectionAuthenticated(XmppConnectionAuthenticatedTrigger trigger)
    {
        try
        {
            ChatHelper.getXmppConnection();
        } catch (Exception e)
        {
            renderNoSessionView();
        }

    }

    @Subscribe
    public void gotXmppConnection(GotXmppConnectionTrigger trigger)
    {
        connection = trigger.getConnection();
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                renderChatView();

                multiUserChat = chatService.getMultiUserChat(connection, eventId);

                if (multiUserChat == null)
                {
                    renderNoSessionView();
                    return;
                } else
                {
                    chatMessageList = new ArrayList<>();
                    messageListener = new MessageListener()
                    {
                        @Override
                        public void processMessage(Message message)
                        {
                            ChatMessage newMessage = new ChatMessage();
                            newMessage.setId(message.getStanzaId());

                            String[] fromUser = message.getFrom().split("/");

                            if (fromUser[1].equals("reap3r"))
                            {
                                newMessage.setMessage(message.getBody());
                                newMessage.setSenderName("reap3r");
                                newMessage.setSenderId("reap3r");
                                newMessage.setMe(false);

                            } else
                            {
                                String[] userDetails = fromUser[1].split("_");
                                newMessage.setMessage(message.getBody());
                                newMessage.setSenderName(userDetails[0]);
                                newMessage.setSenderId(userDetails[1]);

                                if (userService.getActiveUserId().equals(userDetails[1]))
                                {
                                    newMessage.setMe(true);
                                } else
                                {
                                    newMessage.setMe(false);
                                }

                            }

                            chatMessageList.add(newMessage);
                            displayMessage(newMessage);

                        }
                    };
                    multiUserChat.addMessageListener(messageListener);
                }

                initChatAdapter();
                chatAdapter.clear();
                renderHistory();
            }
        });
    }

    @Subscribe
    public void onXmppConnectionNull(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.XMPP_CONNECTION_NULL)
        {
            ChatHelper.init(userService.getActiveUserId());
        }
    }


    @Subscribe
    public void backPressed(BackPressedTrigger trigger)
    {
        if (trigger.getActiveFragment().equals(BackstackTags.CHAT))
        {
            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger)
    {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(eventId);

        int activePosition = events.indexOf(activeEvent);

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }
}
