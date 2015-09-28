package reaper.android.app.ui.screens.home;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventComparator;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.common.ViewPagerStateChangedTrigger;
import reaper.android.app.trigger.event.EventClickTrigger;
import reaper.android.app.trigger.event.EventIdsFetchedTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.event.NewEventsAndUpdatesFetchedTrigger;
import reaper.android.app.trigger.event.RsvpChangeTrigger;
import reaper.android.app.trigger.notifications.NewNotificationReceivedTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsAvailableTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsNotAvailableTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.notifications.NotificationFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class HomeFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentManager fragmentManager;
    private Bus bus;

//    private Sort sort;
//    private Filter filter;

    // Services
    private UserService userService;
    private EventService eventService;
    private LocationService locationService;
    private NotificationService notificationService;

    //Cache
    private GenericCache genericCache;

    // Data
    private List<Event> events;
    private Location userLocation;
    EventCategory eventCategory;

    // UI Elements
    private TextView noEventsMessage;
    private RecyclerView eventList;
    //    private LinearLayout buttonBar;
//    private Button filterButton, sortButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Snackbar snackbar;
    private MaterialIconView generalIcon, eatOutIcon, drinksIcon, cafeIcon, movieIcon, outdoorsIcon, partyIcon, eventsIcon, shoppingIcon;
    private Drawable phoneDrawable, accountsDrawable;
    private Toolbar toolbar;

    private EventsAdapter eventsAdapter;
    private Drawable whiteNotificationdrawable;
    private Menu menu;
    private Drawable greenNotificationdrawable;

    @Override
    public void onRefresh() {
        eventService.getAllEventIds();
    }

    private enum Sort implements Serializable {
        RELEVANCE, DATE_TIME, DISTANCE
    }

    private enum Filter implements Serializable {
        TODAY, ALL
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        eventList = (RecyclerView) view.findViewById(R.id.rv_home_events);
        noEventsMessage = (TextView) view.findViewById(R.id.tv_home_no_events);
//        buttonBar = (LinearLayout) view.findViewById(R.id.ll_home_btn_bar);
//        filterButton = (Button) view.findViewById(R.id.btn_home_filter);
//        sortButton = (Button) view.findViewById(R.id.btn_home_sort);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_home);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_home);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        generateDrawables();
        dispayBasicView();

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        notificationService = new NotificationService(bus);

        genericCache = CacheManager.getGenericCache();
//
//        filterButton.setText(R.string.filter_all);
//        sortButton.setText(R.string.sort_relevance);
//
//        sortButton.setOnClickListener(this);
//        filterButton.setOnClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        events = new ArrayList<>();
        userLocation = locationService.getUserLocation();
        eventCategory = EventCategory.GENERAL;

//        if (savedInstanceState != null)
//        {
//            sort = (Sort) savedInstanceState.getSerializable("sort");
//            filter = (Filter) savedInstanceState.getSerializable("filter");
//        } else
//        {
//            sort = Sort.RELEVANCE;
//            filter = Filter.ALL;
//        }
        initRecyclerView();
    }

    private void generateDrawables() {
        phoneDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        accountsDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        whiteNotificationdrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                .setColor(getResources().getColor(R.color.white))
                .setSizeDp(36)
                .build();

        greenNotificationdrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.BELL)
                .setColor(getResources().getColor(R.color.accent))
                .setSizeDp(36)
                .build();
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState)
//    {
//        super.onSaveInstanceState(outState);
//        outState.putSerializable("sort", sort);
//        outState.putSerializable("filter", filter);
//    }

    @Override
    public void onResume() {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.HOME_FRAGMENT);

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.HOME);

        bus.register(this);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("clanOut");

        if (genericCache.get(CacheKeys.HAS_FETCHED_PENDING_INVITES) == null) {

            displayUpdatePhoneDialog();

        } else {
            eventService.fetchEvents(userLocation.getZone());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);

        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Subscribe
    public void onEventsFetchTrigger(EventsFetchTrigger eventsFetchTrigger) {
        events = eventsFetchTrigger.getEvents();
        refreshRecyclerView();
    }

    @Subscribe
    public void onEventsNotFetchedTrigger(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.EVENTS_FETCH_FAILURE) {
            displayErrorView();
        }
    }

    @Subscribe
    public void onEventClickTrigger(EventClickTrigger eventClickTrigger) {
        Event event = eventClickTrigger.getEvent();
        int activePosition = events.indexOf(event);

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);

        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }

    @Subscribe
    public void onRsvpChanged(RsvpChangeTrigger rsvpChangeTrigger) {
        Event updatedEvent = rsvpChangeTrigger.getUpdatedEvent();
        Event.RSVP oldRsvp = rsvpChangeTrigger.getOldRsvp();

        eventService.updateRsvp(updatedEvent, oldRsvp, true);
    }

    @Subscribe
    public void onGenericErrorTrigger(GenericErrorTrigger trigger) {
        ErrorCode code = trigger.getErrorCode();

        if (code == ErrorCode.RSVP_UPDATE_FAILURE) {
            Snackbar.make(getView(), R.string.message_rsvp_update_failure, Snackbar.LENGTH_LONG).show();
            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }

    @Subscribe
    public void onViewPagerStateChanged(ViewPagerStateChangedTrigger trigger) {
        if (trigger.getState() == ViewPager.SCROLL_STATE_DRAGGING) {
            swipeRefreshLayout.setEnabled(false);
        } else {
            swipeRefreshLayout.setEnabled(true);
        }
    }

    @Subscribe
    public void onEventIdsFetched(EventIdsFetchedTrigger trigger) {
        if (genericCache.get(CacheKeys.LAST_UPDATE_TIMESTAMP) != null) {
            eventService.fetchNewEventsAndUpdatesFromNetwork(locationService.getUserLocation().getZone(), trigger.getEventIdList(), genericCache.get(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.class));
        } else {
            genericCache.put(CacheKeys.LAST_UPDATE_TIMESTAMP, DateTime.now());
        }
    }

    @Subscribe
    public void OnEventIdsNotFetched(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.EVENT_IDS_FETCH_FAILURE) {
            displayErrorView();
        }
    }

    @Subscribe
    public void onNewEventsAndUpdatesFetched(NewEventsAndUpdatesFetchedTrigger trigger) {
        events = trigger.getEventList();

//        sort = Sort.RELEVANCE;
//        filter = Filter.ALL;
//        filterButton.setText("ALL EVENTS");
//        sortButton.setText("RELEVANCE");

        refreshRecyclerView();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void onNewEventsAndUpdatesNotFetched(GenericErrorTrigger trigger) {
        if (trigger.getErrorCode() == ErrorCode.NEW_EVENTS_AND_UPDATES_FETCH_FAILURE) {
            displayErrorView();
        }
    }

    private void initRecyclerView() {
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventsAdapter = new EventsAdapter(bus, new ArrayList<Event>(), getActivity(), fragmentManager);
        eventList.setAdapter(eventsAdapter);

        eventList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                boolean enabled = false;
                if (eventList != null && eventList.getChildCount() > 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) eventList.getLayoutManager();
                    boolean isFirstItemVisible = linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;

                    enabled = isFirstItemVisible;
                }

                swipeRefreshLayout.setEnabled(enabled);
            }
        });
    }

    private void refreshRecyclerView() {
        if (events.size() == 0) {
            displayNoEventsView();
        } else {
            dispayBasicView();
        }

//        switch (sort)
//        {
//            case RELEVANCE:
//                Collections.sort(events, new EventComparator.Relevance(userService.getActiveUserId()));
//                break;
//            case DATE_TIME:
//                Collections.sort(events, new EventComparator.DateTime());
//                break;
//            case DISTANCE:
//                Collections.sort(events, new EventComparator.Distance(userLocation.getLongitude(), userLocation.getLatitude()));
//                break;
//            default:
//                break;
//        }

        Collections.sort(events, new EventComparator.Relevance(userService.getActiveUserId()));

//        if (filter == Filter.ALL)
//        {
//            eventsAdapter = new EventsAdapter(bus, events, getActivity());
//            eventList.setAdapter(eventsAdapter);
//        } else
//        {
//            DateTime now = DateTime.now();
//            DateTime todayEnd = DateTime.now().withHourOfDay(23).withMinuteOfHour(59);
//            List<Event> visibleEvents = new ArrayList<>();
//            for (Event event : events)
//            {
//                if (event.getStartTime().isBefore(todayEnd) && event.getEndTime().isAfter(now))
//                {
//                    visibleEvents.add(event);
//                }
//            }
//
//            if (visibleEvents.size() == 0)
//            {
//                displayNoEventsView();
//            } else
//            {
//                eventsAdapter = new EventsAdapter(bus, visibleEvents, getActivity());
//                eventList.setAdapter(eventsAdapter);
//            }
//        }

        eventsAdapter = new EventsAdapter(bus, events, getActivity(), fragmentManager);
        eventList.setAdapter(eventsAdapter);
    }

    private void dispayBasicView() {
        noEventsMessage.setVisibility(View.GONE);
        eventList.setVisibility(View.VISIBLE);
//        buttonBar.setVisibility(View.VISIBLE);
    }

    private void displayNoEventsView() {
        noEventsMessage.setText(R.string.home_no_events);
        noEventsMessage.setVisibility(View.VISIBLE);
        eventList.setVisibility(View.GONE);
//        buttonBar.setVisibility(View.VISIBLE);
    }

    private void displayErrorView() {
        noEventsMessage.setVisibility(View.VISIBLE);
        noEventsMessage.setText(R.string.home_events_fetch_error);
        eventList.setVisibility(View.GONE);
//        buttonBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        this.menu = menu;
        notificationService.areNewNotificationsAvailable();

        menu.findItem(R.id.action_account).setVisible(true);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_notifications).setVisible(true);

        menu.findItem(R.id.action_account).setIcon(accountsDrawable);

        menu.findItem(R.id.action_notifications).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FragmentUtils.changeFragment(fragmentManager, new NotificationFragment());
                return true;
            }
        });

        if (genericCache.get(CacheKeys.MY_PHONE_NUMBER) == null) {
            menu.findItem(R.id.action_add_phone).setVisible(true);
            menu.findItem(R.id.action_add_phone).setIcon(phoneDrawable);

            menu.findItem(R.id.action_add_phone).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
                    builder.setView(dialogView);

                    final EditText phoneNumber = (EditText) dialogView.findViewById(R.id.et_alert_dialog_add_phone);

                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Boolean wantToCloseDialog = false;
                            String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText().toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                            if (parsedPhone == null) {
                                Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG).show();
                                wantToCloseDialog = false;
                            } else {
                                userService.updatePhoneNumber(parsedPhone);

                                menu.findItem(R.id.action_add_phone).setVisible(false);

                                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(dialogView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                wantToCloseDialog = true;

                            }

                            if (wantToCloseDialog) {
                                alertDialog.dismiss();
                            }
                        }
                    });

                    return true;
                }

            });
        } else {
            menu.findItem(R.id.action_add_phone).setVisible(false);
        }

        menu.findItem(R.id.action_account).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
//        if (view.getId() == R.id.btn_home_filter)
//        {
//            PopupMenu filterMenu = new PopupMenu(getActivity(), filterButton);
//            filterMenu.getMenuInflater().inflate(R.menu.popup_filter, filterMenu.getMenu());
//
//            filterMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
//            {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem)
//                {
//                    if (menuItem.getItemId() == R.id.menu_filter_today)
//                    {
//                        filterButton.setText(menuItem.getTitle().toString());
//                        filter = Filter.TODAY;
//                        refreshRecyclerView();
//                    } else if (menuItem.getItemId() == R.id.menu_filter_all)
//                    {
//                        filterButton.setText(menuItem.getTitle().toString());
//                        filter = Filter.ALL;
//                        refreshRecyclerView();
//                    }
//                    return true;
//                }
//            });
//
//            filterMenu.show();
//        } else if (view.getId() == R.id.btn_home_sort)
//        {
//            PopupMenu sortMenu = new PopupMenu(getActivity(), sortButton);
//            sortMenu.getMenuInflater().inflate(R.menu.popup_sort, sortMenu.getMenu());
//
//            sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
//            {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem)
//                {
//                    if (menuItem.getItemId() == R.id.menu_sort_relevance)
//                    {
//                        sortButton.setText(menuItem.getTitle().toString());
//                        sort = Sort.RELEVANCE;
//                        refreshRecyclerView();
//                    } else if (menuItem.getItemId() == R.id.menu_sort_time)
//                    {
//                        sortButton.setText(menuItem.getTitle().toString());
//                        sort = Sort.DATE_TIME;
//                        refreshRecyclerView();
//                    } else if (menuItem.getItemId() == R.id.menu_sort_distance)
//                    {
//                        sortButton.setText(menuItem.getTitle().toString());
//                        sort = Sort.DISTANCE;
//                        refreshRecyclerView();
//                    }
//                    return true;
//                }
//            });
//
//            sortMenu.show();
    }

    @Subscribe
    public void newNotificationsAvailable(NewNotificationsAvailableTrigger trigger) {
        menu.findItem(R.id.action_notifications).setIcon(greenNotificationdrawable);
    }

    @Subscribe
    public void newNotificationsNotAvailable(NewNotificationsNotAvailableTrigger trigger) {
        menu.findItem(R.id.action_notifications).setIcon(whiteNotificationdrawable);
    }

    @Subscribe
    public void newNotificationReceived(NewNotificationReceivedTrigger trigger) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                menu.findItem(R.id.action_notifications).setIcon(greenNotificationdrawable);
            }
        });
    }

    private void displayUpdatePhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.fetch_pending_invites_title);
        builder.setMessage(R.string.fetch_pending_invites_message);
        builder.setCancelable(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView.findViewById(R.id.et_alert_dialog_add_phone);

        builder.setPositiveButton(R.string.fetch_pending_invites_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(R.string.fetch_pending_invites_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                genericCache.put(CacheKeys.HAS_FETCHED_PENDING_INVITES, true);

                eventService.fetchEvents(locationService.getUserLocation().getZone());
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText().toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                if (parsedPhone == null) {
                    Snackbar.make(getView(), R.string.phone_invalid, Snackbar.LENGTH_LONG).show();
                    wantToCloseDialog = false;
                } else {
                    userService.updatePhoneNumber(parsedPhone);

                    userService.fetchPendingInvites(parsedPhone, locationService.getUserLocation().getZone());

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(dialogView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    wantToCloseDialog = true;
                }

                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });

    }
}
