package reaper.android.app.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventComparator;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.EventClickTrigger;
import reaper.android.app.trigger.EventUpdatesFetchTrigger;
import reaper.android.app.trigger.EventsFetchTrigger;
import reaper.android.app.trigger.GenericErrorTrigger;
import reaper.android.app.trigger.RsvpChangeTrigger;
import reaper.android.app.ui.details.EventDetailsFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

public class HomeFragment extends Fragment implements View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;
    private Timer timer;

    private Sort sort;
    private Filter filter;

    // Services
    private UserService userService;
    private EventService eventService;
    private LocationService locationService;

    // Data
    private List<Event> events;
    private List<String> eventUpdates;
    private List<String> chatUpdates;
    private Location userLocation;

    // UI Elements
    private TextView noEventsMessage;
    private RecyclerView eventList;
    private LinearLayout buttonBar;
    private Button filterButton, sortButton;
    private MenuItem refresh;

    private EventsAdapter eventsAdapter;

    private static enum Sort
    {
        RELEVANCE, DATE_TIME, DISTANCE
    }

    private static enum Filter
    {
        TODAY, ALL
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        eventList = (RecyclerView) view.findViewById(R.id.rv_home_events);
        noEventsMessage = (TextView) view.findViewById(R.id.tv_home_no_events);
        buttonBar = (LinearLayout) view.findViewById(R.id.ll_home_btn_bar);
        filterButton = (Button) view.findViewById(R.id.btn_home_filter);
        sortButton = (Button) view.findViewById(R.id.btn_home_sort);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);

        filterButton.setText("All Events");
        sortButton.setText("Relevance");

        sortButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);

        events = new ArrayList<>();
        eventUpdates = new ArrayList<>();
        chatUpdates = new ArrayList<>();
        userLocation = locationService.getUserLocation();

        sort = Sort.RELEVANCE;
        filter = Filter.ALL;

        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);
        eventService.fetchEvents(userLocation.getZone());

        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                DateTime lastUpdated = (DateTime) Cache.getInstance().get(CacheKeys.EVENTS_TIMESTAMP);
                eventService.fetchEventUpdates(userLocation.getZone(), lastUpdated);
            }
        }, AppConstants.EVENTS_REFRESH_RATE_MILLISECONDS, AppConstants.EVENTS_REFRESH_RATE_MILLISECONDS);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        timer.cancel();
        bus.unregister(this);
    }

    @Subscribe
    public void onEventsFetchTrigger(EventsFetchTrigger eventsFetchTrigger)
    {
        events = eventsFetchTrigger.getEvents();
        eventUpdates = eventService.getUpdatedEvents();

        refreshRecyclerView();
    }

    @Subscribe
    public void onEventUpdatesFetchTrigger(EventUpdatesFetchTrigger eventUpdatesFetchTrigger)
    {
        if (eventUpdatesFetchTrigger.getEventUpdates() != null)
        {
            events = eventUpdatesFetchTrigger.getEventUpdates();
            eventUpdates = eventService.getUpdatedEvents();

            if (refresh != null)
            {
                refresh.setVisible(true);
            }
            else
            {
                refreshRecyclerView();
            }
        }
    }

    @Subscribe
    public void onEventClickTrigger(EventClickTrigger eventClickTrigger)
    {
        Event event = eventClickTrigger.getEvent();

        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        eventDetailsFragment.setArguments(bundle);

        FragmentUtils.changeFragment(fragmentManager, eventDetailsFragment);
    }

    @Subscribe
    public void onRsvpChangeTrigger(RsvpChangeTrigger rsvpChangeTrigger)
    {
        Event updatedEvent = rsvpChangeTrigger.getUpdatedEvent();
        Event.RSVP oldRsvp = rsvpChangeTrigger.getOldRsvp();

        eventService.updateRsvp(updatedEvent, oldRsvp);
    }

    @Subscribe
    public void onGenericErrorTrigger(GenericErrorTrigger trigger)
    {
        ErrorCode code = trigger.getErrorCode();

        if (code == ErrorCode.RSVP_UPDATE_FAILURE)
        {
            Toast.makeText(getActivity(), "RSVP update failed. Please try again Later", Toast.LENGTH_LONG).show();
            refreshRecyclerView();
        }
    }

    private void initRecyclerView()
    {
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventsAdapter = new EventsAdapter(bus, new ArrayList<Event>(), new ArrayList<String>(), new ArrayList<String>());
        eventList.setAdapter(eventsAdapter);
    }

    private void refreshRecyclerView()
    {
        if (refresh != null)
        {
            refresh.setVisible(false);
        }

        if (events.size() == 0)
        {
            setNoEventsView();
        }
        else
        {
            setNormalView();
        }

        switch (sort)
        {
            case RELEVANCE:
                Collections.sort(events, new EventComparator.Relevance(userService.getActiveUser()));
                break;
            case DATE_TIME:
                Collections.sort(events, new EventComparator.DateTime());
                break;
            case DISTANCE:
                Collections.sort(events, new EventComparator.Distance(userLocation.getLongitude(), userLocation.getLatitude()));
                break;
            default:
                break;
        }

        if (filter == Filter.ALL)
        {
            eventsAdapter = new EventsAdapter(bus, events, eventUpdates, chatUpdates);
            eventList.setAdapter(eventsAdapter);
        }
        else
        {
            DateTime now = DateTime.now();
            DateTime todayEnd = DateTime.now().withHourOfDay(23).withMinuteOfHour(59);
            List<Event> visibleEvents = new ArrayList<>();
            for (Event event : events)
            {
                if (event.getStartTime().isBefore(todayEnd) && event.getEndTime().isAfter(now))
                {
                    visibleEvents.add(event);
                }
            }

            eventsAdapter = new EventsAdapter(bus, visibleEvents, eventUpdates, chatUpdates);
            eventList.setAdapter(eventsAdapter);
        }
    }

    private void setNormalView()
    {
        noEventsMessage.setVisibility(View.GONE);
        eventList.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
    }

    private void setNoEventsView()
    {
        noEventsMessage.setText("No events to show");
        noEventsMessage.setVisibility(View.VISIBLE);
        eventList.setVisibility(View.GONE);
        buttonBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.abbAccounts).setVisible(true);
        menu.findItem(R.id.abbCreateEvent).setVisible(true);
        menu.findItem(R.id.abbRefresh).setVisible(false);
        menu.findItem(R.id.abbHome).setVisible(false);
        menu.findItem(R.id.abbEditEvent).setVisible(false);
        menu.findItem(R.id.abbSearch).setVisible(false);
        menu.findItem(R.id.abbFinaliseEvent).setVisible(false);
        menu.findItem(R.id.abbDeleteEvent).setVisible(false);

        menu.findItem(R.id.abbAccounts).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                Toast.makeText(getActivity(), "Accounts Page", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        menu.findItem(R.id.abbCreateEvent).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                Toast.makeText(getActivity(), "Create Event", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        refresh = menu.findItem(R.id.abbRefresh);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                refreshRecyclerView();
                return true;
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btn_home_filter)
        {
            PopupMenu filterMenu = new PopupMenu(getActivity(), filterButton);
            filterMenu.getMenuInflater().inflate(R.menu.popup_filter_menu, filterMenu.getMenu());

            filterMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (menuItem.getItemId() == R.id.itemToday)
                    {
                        filterButton.setText(menuItem.getTitle().toString());
                        filter = Filter.TODAY;
                        refreshRecyclerView();
                    }

                    if (menuItem.getItemId() == R.id.itemAllEvents)
                    {
                        filterButton.setText(menuItem.getTitle().toString());
                        filter = Filter.ALL;
                        refreshRecyclerView();
                    }
                    return true;
                }
            });

            filterMenu.show();
        }

        if (view.getId() == R.id.btn_home_sort)
        {
            PopupMenu sortMenu = new PopupMenu(getActivity(), sortButton);
            sortMenu.getMenuInflater().inflate(R.menu.popup_sort_menu, sortMenu.getMenu());

            sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (menuItem.getItemId() == R.id.itemRelevance)
                    {
                        sortButton.setText(menuItem.getTitle().toString());
                        sort = Sort.RELEVANCE;
                        refreshRecyclerView();
                    }

                    if (menuItem.getItemId() == R.id.itemStartTime)
                    {
                        sortButton.setText(menuItem.getTitle().toString());
                        sort = Sort.DATE_TIME;
                        refreshRecyclerView();
                    }

                    if (menuItem.getItemId() == R.id.itemDistance)
                    {
                        sortButton.setText(menuItem.getTitle().toString());
                        sort = Sort.DISTANCE;
                        refreshRecyclerView();
                    }
                    return true;
                }
            });

            sortMenu.show();
        }
    }
}
