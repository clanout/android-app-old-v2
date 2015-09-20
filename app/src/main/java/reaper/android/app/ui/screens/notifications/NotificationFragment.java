package reaper.android.app.ui.screens.notifications;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

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
import reaper.android.app.model.Event;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.notifications.NotificationsFetchedTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.notification.Notification;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationFragment extends BaseFragment implements NotificationClickCommunicator, View.OnClickListener
{
    private RecyclerView notificationRecyclerView;
    private TextView noNotificationsMessage, clearAll;
    private NotificationsAdapter notificationsAdapter;
    private Menu menu;
    private Drawable homeDrawable;
    private Toolbar toolbar;
    private NotificationService notificationService;
    private Bus bus;
    private EventService eventService;
    private LocationService locationService;
    private Notification notification;
    private ItemTouchHelper itemTouchHelper;
    private List<Notification> notificationList;
    private GenericCache genericCache;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        notificationRecyclerView = (RecyclerView) view.findViewById(R.id.rv_fragment_notificatiosn);
        noNotificationsMessage = (TextView) view.findViewById(R.id.tv_fragemnt_notification_no_notifications);
        clearAll = (TextView) view.findViewById(R.id.tv_fragment_notification_clear_all);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_notifications);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);

        bus = Communicator.getInstance().getBus();
        notificationService = new NotificationService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        genericCache = CacheManager.getGenericCache();

        clearAll.setOnClickListener(this);

        notificationList = new ArrayList<>();

        notificationService.markAllNotificationsAsRead();

        generateDrawables();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                notificationService.deleteNotificationFromCache(notificationList.get(viewHolder.getAdapterPosition()).getId());
                notificationList.remove(viewHolder.getAdapterPosition());
                refreshRecyclerView();
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        initRecyclerView();
        displayBasicView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.NOTIFICATIONS);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.NOTIFICATION_FRAGMENT);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Notifications");

        bus.register(this);
        notificationService.fetchAllNotifications();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        this.menu = menu;

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(true);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(false);

        menu.findItem(R.id.action_home).setIcon(homeDrawable);

        menu.findItem(R.id.action_home).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                FragmentUtils.changeFragment(getActivity().getFragmentManager(), new HomeFragment());
                return true;
            }
        });
    }

    @Override
    public void onNotificationClicked(Notification notification)
    {
        this.notification = notification;
        notificationService.deleteNotificationFromCache(notification.getId());
        eventService.fetchEvents(locationService.getUserLocation().getZone());
    }

    private void displayBasicView()
    {
        notificationRecyclerView.setVisibility(View.VISIBLE);
        noNotificationsMessage.setVisibility(View.GONE);
    }

    private void displayErrorView()
    {
        notificationRecyclerView.setVisibility(View.GONE);
        noNotificationsMessage.setVisibility(View.VISIBLE);
        clearAll.setVisibility(View.GONE);
        noNotificationsMessage.setText("Could not load notifications");
    }

    private void displayNoNotificationsView()
    {
        notificationRecyclerView.setVisibility(View.GONE);
        noNotificationsMessage.setVisibility(View.VISIBLE);
        clearAll.setVisibility(View.GONE);
        noNotificationsMessage.setText("No notifications to show");
    }

    private void initRecyclerView()
    {
        notificationsAdapter = new NotificationsAdapter(getActivity(), notificationList);
        notificationsAdapter.setCommunicator(this);

        notificationRecyclerView.setAdapter(notificationsAdapter);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);
    }

    private void refreshRecyclerView()
    {
        notificationsAdapter = new NotificationsAdapter(getActivity(), notificationList);
        notificationsAdapter.setCommunicator(this);
        notificationRecyclerView.setAdapter(notificationsAdapter);
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);

        if (notificationList.size() == 0)
        {
            displayNoNotificationsView();

        } else
        {
            displayBasicView();
        }
    }

    private void generateDrawables()
    {
        homeDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.HOME)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();
    }

    @Subscribe
    public void onNotificationsFetched(NotificationsFetchedTrigger trigger)
    {
        notificationList = trigger.getNotifications();
        refreshRecyclerView();
    }

    @Subscribe
    public void onNotificationsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.NOTIFICATIONS_FETCH_FAILURE)
        {
            displayErrorView();
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger)
    {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(notification.getEventId());

        int activePosition = 0;

        if (events.contains(activeEvent))
        {
            activePosition = events.indexOf(activeEvent);
        } else
        {
            Toast.makeText(getActivity(), "Could not find this event in the list", Toast.LENGTH_LONG).show();
            return;
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(getActivity().getFragmentManager(), eventDetailsContainerFragment);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.tv_fragment_notification_clear_all)
        {
            notificationService.deleteAllNotificationsFromCache();
            notificationList = new ArrayList<>();
            refreshRecyclerView();
        }
    }
}
