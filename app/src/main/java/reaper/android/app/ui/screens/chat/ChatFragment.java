package reaper.android.app.ui.screens.chat;

import android.app.FragmentManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

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
import reaper.android.app.service.ChatService;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.chat.GotXmppConnectionTrigger;
import reaper.android.app.trigger.chat.XmppConnectionAuthenticatedTrigger;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;


/**
 * Created by harsh on 21-05-2015.
 */
public class ChatFragment extends BaseFragment implements View.OnClickListener {

    private EditText typeMessage;
    private MaterialIconView send;
    private ListView listView;
    private TextView noSessionMessage;
    private LinearLayout mainContent, loading;
    private ProgressBar progressBar;
    private MenuItem loadHistory;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private String eventId;
    private String eventName;
    private boolean isMessageSent;

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
    private Toolbar toolbar;

    private long loadHistoryLastClickedTime = 0;

    private TextWatcher chatWatcher;

    private String stanzaId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.CHAT_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        typeMessage = (EditText) view.findViewById(R.id.et_chat_fragment_type_message_chat);
        send = (MaterialIconView) view.findViewById(R.id.ib_fragment_chat_send);
        listView = (ListView) view.findViewById(R.id.lv_chat_fragment);
        noSessionMessage = (TextView) view.findViewById(R.id.tv_fragment_chat_no_session);
        mainContent = (LinearLayout) view.findViewById(R.id.ll_fragment_chat_main_content);
        loading = (LinearLayout) view.findViewById(R.id.ll_fragment_chat_loading);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_chat);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_fragment_chat);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        renderLoadingView();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);

        send.setOnClickListener(this);

        send.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));

        Bundle bundle = getArguments();
        if (bundle == null) {

            renderNoSessionView();
        }

        eventId = (String) bundle.get(BundleKeys.CHAT_FRAGMENT_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {

            renderNoSessionView();
        }

        eventName = (String) bundle.get(BundleKeys.CHAT_FRAGMENT_EVENT_NAME);
        if (eventName == null) {

            renderNoSessionView();
        }

        bus = Communicator.getInstance().getBus();
        userService = new UserService(bus);
        chatService = new ChatService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getActivity().getFragmentManager();

        genericCache = CacheManager.getGenericCache();

        isMessageSent = false;

        chatMessageList = new ArrayList<>();

        chatWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    send.setColor(ContextCompat.getColor(getActivity(), R.color.accent));
                } else {
                    send.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                Log.d("APP", "list size --- " + chatMessageList.size() + " click count --- " + loadHistoryClickCount);

                if (chatMessageList.size() >= 20 * loadHistoryClickCount) {
                    Log.d("APP", "1st condition successful");
                    if (firstVisibleItem == 0) {
                        Log.d("APP", "2nd condition successful ---- first visible item ---- " + firstVisibleItem);
                        loadHistory.setVisible(true);

                        Log.d("APP", "both success --- " + loadHistory.toString() + " title --- " + loadHistory.getTitle());
                    } else {
                        Log.d("APP", "2nd condition not successful ---- first visible item ---- " + firstVisibleItem);
                        loadHistory.setVisible(false);
                    }
                } else {

                    if (loadHistory != null) {
                        loadHistory.setVisible(false);
                    }

                    Log.d("APP", "1st condition not successful ---- first visible item ---- " + firstVisibleItem);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_chat);

        bus.register(this);

        try {
            ChatHelper.getXmppConnection();
        } catch (Exception e) {

            Log.d("APP", "error -- exception getXmppConnection" + e.getMessage());

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_GET_XMPP_CONNECTION, userService.getActiveUserId());

            renderNoSessionView();
        }

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.CHAT);

        typeMessage.addTextChangedListener(chatWatcher);
    }

    private void renderNoSessionView() {
        mainContent.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        noSessionMessage.setVisibility(View.VISIBLE);
        noSessionMessage.setText(R.string.chat_messages_not_fetched);
        return;
    }

    private void renderChatView() {
        mainContent.setVisibility(View.VISIBLE);
        noSessionMessage.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    private void initChatAdapter() {
        chatAdapter = new ChatAdapter(new ArrayList<ChatMessage>(), getActivity());
        listView.setAdapter(chatAdapter);
    }

    private void renderLoadingView() {
        mainContent.setVisibility(View.GONE);
        noSessionMessage.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_chat, menu);

        loadHistory = menu.findItem(R.id.action_load_history_chat);
        loadHistory.setVisible(false);

        Log.d("APP", "onCreateOptionsMenu --- " + loadHistory.toString() + " title --- " + loadHistory.getTitle());

        loadHistory.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if ((SystemClock.elapsedRealtime() - loadHistoryLastClickedTime) < 1000) {
                    return true;
                }

                loadHistoryLastClickedTime = SystemClock.elapsedRealtime();
                item.setEnabled(false);

                loadHistoryClickCount++;

                chatMessageList = new ArrayList<>();
                chatAdapter.clear();

                int maxStanzas = 20 * loadHistoryClickCount;

                try {
                    chatService.fetchHistory(multiUserChat, userService.getActiveUserName() + "_" + userService.getActiveUserId(), userService.getActiveUserId(), connection.getPacketReplyTimeout(), maxStanzas);
                } catch (Exception e) {

                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_LOAD_CHAT_HISTORY, userService.getActiveUserId());

                    renderNoSessionView();
                }
                item.setEnabled(true);

                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (messageListener != null) {
            multiUserChat.removeMessageListener(messageListener);
            messageListener = null;
        }

        typeMessage.removeTextChangedListener(chatWatcher);
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    public void displayMessage(final ChatMessage message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                chatAdapter.add(message);
                chatAdapter.notifyDataSetChanged();
                scroll();
            }
        });
    }

    private void scroll() {
        listView.setSelection(listView.getCount() - 1);
    }

    private void renderHistory() {
        try {
            chatService.fetchHistory(multiUserChat, userService.getActiveUserName() + "_" + userService.getActiveUserId(), userService.getActiveUserId(), connection.getPacketReplyTimeout(), 20);
        } catch (Exception e) {

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_LOAD_CHAT_HISTORY, userService.getActiveUserId());

            renderNoSessionView();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_fragment_chat_send) {

            Log.d("APP", "start--- " + System.currentTimeMillis() +"");

            String message = typeMessage.getText().toString();
            if (TextUtils.isEmpty(message)) {
                return;
            }


            try {

                StringBuilder stringBuilder = new StringBuilder(userService.getActiveUserId());
                stringBuilder.append(eventId);
                stringBuilder.append(System.currentTimeMillis());

                stanzaId = stringBuilder.toString();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMessage(message);
                chatMessage.setSenderId(userService.getActiveUserId());
                chatMessage.setMe(true);
                chatMessage.setSenderName(userService.getActiveUserName());
                chatMessage.setId(stanzaId);
                displayMessage(chatMessage);
                Log.d("APP", "end -- " + System.currentTimeMillis() + "");

                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.CHAT_MESSAGE_SENDING_ATTEMPT, userService.getActiveUserId());
                Message messagePacket = new Message();
                messagePacket.setBody(message);
                messagePacket.setStanzaId(stanzaId);


                chatMessageList.add(chatMessage);

                chatService.postMessage(multiUserChat, messagePacket, userService.getActiveUserName() + "_" + userService.getActiveUserId(), connection.getPacketReplyTimeout());
            } catch (Exception e) {

                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_SEND_CHAT_MESSAGE, userService.getActiveUserId());

                typeMessage.setText(message);
                Snackbar.make(getView(), R.string.chat_message_not_sent, Snackbar.LENGTH_LONG).show();
                return;
            }

            isMessageSent = true;

            typeMessage.setText("");

        }
    }

    @Subscribe
    public void xmppConnectionAuthenticated(XmppConnectionAuthenticatedTrigger trigger) {
        try {
            ChatHelper.getXmppConnection();
        } catch (Exception e) {

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.COULD_NOT_GET_XMPP_CONNECTION, userService.getActiveUserId());

            renderNoSessionView();
        }

    }

    @Subscribe
    public void gotXmppConnection(GotXmppConnectionTrigger trigger) {
        connection = trigger.getConnection();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderChatView();

                multiUserChat = chatService.getMultiUserChat(connection, eventId);

                if (multiUserChat == null) {
                    renderNoSessionView();
                    return;
                } else {
                    chatMessageList = new ArrayList<>();
                    messageListener = new MessageListener() {
                        @Override
                        public void processMessage(Message message) {
                            ChatMessage newMessage = new ChatMessage();
                            newMessage.setId(message.getStanzaId());

                            String[] fromUser = message.getFrom().split("/");

                            if (fromUser[1].equals("clanout")) {
                                newMessage.setMessage(message.getBody());
                                newMessage.setSenderName("clanout");
                                newMessage.setSenderId("clanout");
                                newMessage.setMe(false);

                            } else {
                                String[] userDetails = fromUser[1].split("_");
                                newMessage.setMessage(message.getBody());
                                newMessage.setSenderName(userDetails[0]);
                                newMessage.setSenderId(userDetails[1]);

                                if (userService.getActiveUserId().equals(userDetails[1])) {
                                    newMessage.setMe(true);
                                } else {
                                    newMessage.setMe(false);
                                }

                            }

                            if (!(newMessage.getId().equals(stanzaId))) {
                                chatMessageList.add(newMessage);
                                displayMessage(newMessage);
                            }

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
    public void onXmppConnectionNull(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.XMPP_CONNECTION_NULL) {
            ChatHelper.init(userService.getActiveUserId());
        }
    }


    @Subscribe
    public void backPressed(BackPressedTrigger trigger) {
        if (trigger.getActiveFragment().equals(BackstackTags.CHAT)) {
            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger) {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(eventId);

        int activePosition = events.indexOf(activeEvent);

        if (isMessageSent) {
            chatService.sendChatNotification(eventId, eventName);
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            ((MainActivity) getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
