package reaper.android.app.ui.screens.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.notifications.NotificationsFetchedTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.chat.ChatFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SnackbarFactory;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.notification.Notification;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationFragment extends BaseFragment implements NotificationClickCommunicator, View.OnClickListener {
    private RecyclerView notificationRecyclerView;
    private TextView noNotificationsMessage;
    private NotificationsAdapter notificationsAdapter;
    private Toolbar toolbar;
    private NotificationService notificationService;
    private Bus bus;
    private EventService eventService;
    private Notification notification;
    private ItemTouchHelper itemTouchHelper;
    private List<Notification> notificationList;
    private GenericCache genericCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.NOTIFICATION_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        notificationRecyclerView = (RecyclerView) view.findViewById(R.id.rv_fragment_notificatiosn);
        noNotificationsMessage = (TextView) view
                .findViewById(R.id.tv_fragemnt_notification_no_notifications);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_notifications);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bus = Communicator.getInstance().getBus();
        notificationService = new NotificationService(bus);
        eventService = new EventService(bus);
        genericCache = CacheManager.getGenericCache();

        notificationList = new ArrayList<>();

        notificationService.markAllNotificationsAsRead();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                notificationService.deleteNotificationFromCache(notificationList
                        .get(viewHolder.getAdapterPosition()).getId());
                notificationList.remove(viewHolder.getAdapterPosition());
                notificationRecyclerView.getAdapter()
                        .notifyItemRemoved(viewHolder.getAdapterPosition());

                if(notificationList.size() == 0)
                {
                    displayNoNotificationsView();
                }
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        initRecyclerView();
        displayBasicView();
    }

    @Override
    public void onResume() {
        super.onResume();

        genericCache.put(GenericCacheKeys.ACTIVE_FRAGMENT, BackstackTags.NOTIFICATIONS);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_notification);

        bus.register(this);
        notificationService.fetchAllNotifications();
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_notification, menu);

        MenuItem clear = menu.findItem(R.id.action_clear);
        clear.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                notificationService.deleteAllNotificationsFromCache();
                notificationList = new ArrayList<>();
                refreshRecyclerView();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotificationClicked(Notification notification) {
        this.notification = notification;

        notificationService.deleteNotificationFromCache(notification.getId());
        eventService.fetchEvents(LocationService_.getInstance().getCurrentLocation().getZone());
    }

    private void displayBasicView() {
        notificationRecyclerView.setVisibility(View.VISIBLE);
        noNotificationsMessage.setVisibility(View.GONE);
    }

    private void displayErrorView() {
        notificationRecyclerView.setVisibility(View.GONE);
        noNotificationsMessage.setVisibility(View.VISIBLE);
        noNotificationsMessage.setText(R.string.notifications_fetch_failure);
    }

    private void displayNoNotificationsView() {
        notificationRecyclerView.setVisibility(View.GONE);
        noNotificationsMessage.setVisibility(View.VISIBLE);
        noNotificationsMessage.setText(R.string.no_notifications);
    }

    private void initRecyclerView() {
        notificationsAdapter = new NotificationsAdapter(getActivity(), notificationList);
        notificationsAdapter.setCommunicator(this);

        notificationRecyclerView.setAdapter(notificationsAdapter);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);
    }

    private void refreshRecyclerView() {
        notificationsAdapter = new NotificationsAdapter(getActivity(), notificationList);
        notificationsAdapter.setCommunicator(this);
        notificationRecyclerView.setAdapter(notificationsAdapter);
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);

        if (notificationList.size() == 0) {
            displayNoNotificationsView();

        } else {
            displayBasicView();
        }
    }

    @Subscribe
    public void onNotificationsFetched(NotificationsFetchedTrigger trigger) {
        notificationList = trigger.getNotifications();

        refreshRecyclerView();
    }

    @Subscribe
    public void onNotificationsNotFetched(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.NOTIFICATIONS_FETCH_FAILURE) {
            displayErrorView();
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger) {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(notification.getEventId());

        int activePosition = 0;

        if (events.contains(activeEvent)) {
            activePosition = events.indexOf(activeEvent);
        } else {
            SnackbarFactory.create(getActivity(), R.string.error_event_not_found);
            return;
        }

        if (notification.getType() == Notification.CHAT) {

            ChatFragment chatFragment = new ChatFragment();
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_ID, events.get(activePosition).getId());
            bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_NAME, events.get(activePosition).getTitle());
            chatFragment.setArguments(bundle);
            FragmentUtils.changeFragment(getFragmentManager(), chatFragment);

        } else {

            EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
            bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
            eventDetailsContainerFragment.setArguments(bundle);
            FragmentUtils
                    .changeFragment(getActivity().getFragmentManager(), eventDetailsContainerFragment);
        }
    }

    @Override
    public void onClick(View v) {
    }
}
