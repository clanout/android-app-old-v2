package reaper.android.app.ui.screens.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import reaper.android.R;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventComparator;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventClickTrigger;
import reaper.android.app.trigger.event.EventUpdatesFetchTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.event.RsvpChangeTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.create.CreateEventFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.PhoneUtils;
import reaper.android.common.cache.AppPreferences;
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
    EventCategory eventCategory;

    // UI Elements
    private TextView noEventsMessage;
    private RecyclerView eventList;
    private LinearLayout buttonBar;
    private Button filterButton, sortButton;
    private ImageButton refresh;
    private FloatingActionButton createEvent;

    private EventsAdapter eventsAdapter;

    private enum Sort implements Serializable
    {
        RELEVANCE, DATE_TIME, DISTANCE
    }

    private enum Filter implements Serializable
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
        refresh = (ImageButton) view.findViewById(R.id.ibtn_home_refresh);
        createEvent = (FloatingActionButton) view.findViewById(R.id.fib_home_create);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        dispayBasicView();

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);

        filterButton.setText(R.string.filter_all);
        sortButton.setText(R.string.sort_relevance);

        sortButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);
        refresh.setOnClickListener(this);
        createEvent.setOnClickListener(this);

        events = new ArrayList<>();
        eventUpdates = new ArrayList<>();
        chatUpdates = new ArrayList<>();
        userLocation = locationService.getUserLocation();
        eventCategory = EventCategory.GENERAL;

        if (savedInstanceState != null)
        {
            sort = (Sort) savedInstanceState.getSerializable("sort");
            filter = (Filter) savedInstanceState.getSerializable("filter");
        }
        else
        {
            sort = Sort.RELEVANCE;
            filter = Filter.ALL;
        }

        initRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("sort", sort);
        outState.putSerializable("filter", filter);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AppPreferences.set(getActivity(), CacheKeys.ACTIVE_FRAGMENT, BackstackTags.HOME);

        bus.register(this);
        eventService.fetchEvents(userLocation.getZone());

//        timer = new Timer();
//        timer.schedule(new TimerTask()
//        {
//            @Override
//            public void run()
//            {
//                DateTime lastUpdated = (DateTime) Cache.getInstance().get(CacheKeys.EVENTS_TIMESTAMP);
//                eventService.fetchEventUpdates(userLocation.getZone(), lastUpdated);
//            }
//        }, AppConstants.EVENTS_REFRESH_RATE_MILLISECONDS, AppConstants.EVENTS_REFRESH_RATE_MILLISECONDS);
    }

    @Override
    public void onPause()
    {
        super.onPause();
//        timer.cancel();
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
    public void onEventsNotFetchedTrigger(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENTS_FETCH_FAILURE)
        {
            displayErrorView();
        }
    }

//    @Subscribe
//    public void onEventUpdatesFetchTrigger(EventUpdatesFetchTrigger eventUpdatesFetchTrigger)
//    {
//        if (eventUpdatesFetchTrigger.getEventUpdates() != null)
//        {
//            events = eventUpdatesFetchTrigger.getEventUpdates();
//            eventUpdates = eventService.getUpdatedEvents();
//
//            if (refresh != null)
//            {
//                refresh.setVisibility(View.VISIBLE);
//            }
//            else
//            {
//                refreshRecyclerView();
//            }
//        }
//    }

    @Subscribe
    public void onEventClickTrigger(EventClickTrigger eventClickTrigger)
    {
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
            Toast.makeText(getActivity(), R.string.message_rsvp_update_failure, Toast.LENGTH_LONG).show();
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
            refresh.setVisibility(View.INVISIBLE);
        }

        if (events.size() == 0)
        {
            displayNoEventsView();
        }
        else
        {
            dispayBasicView();
        }

        switch (sort)
        {
            case RELEVANCE:
                Collections.sort(events, new EventComparator.Relevance(userService.getActiveUserId()));
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

            if (visibleEvents.size() == 0)
            {
                displayNoEventsView();
            }
            else
            {
                eventsAdapter = new EventsAdapter(bus, visibleEvents, eventUpdates, chatUpdates);
                eventList.setAdapter(eventsAdapter);
            }
        }
    }

    private void dispayBasicView()
    {
        noEventsMessage.setVisibility(View.GONE);
        createEvent.setVisibility(View.GONE);
        eventList.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
    }

    private void displayNoEventsView()
    {
        noEventsMessage.setText(R.string.home_no_events);
        noEventsMessage.setVisibility(View.VISIBLE);
        createEvent.setVisibility(View.VISIBLE);
        eventList.setVisibility(View.GONE);
        buttonBar.setVisibility(View.VISIBLE);
    }

    private void displayErrorView()
    {
        noEventsMessage.setVisibility(View.VISIBLE);
        noEventsMessage.setText(R.string.home_events_fetch_error);
        createEvent.setVisibility(View.GONE);
        eventList.setVisibility(View.GONE);
        buttonBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(true);
        menu.findItem(R.id.action_create_event).setVisible(true);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);

        if (Cache.getInstance().get(CacheKeys.MY_PHONE_NUMBER) == null)
        {
            menu.findItem(R.id.action_add_phone).setVisible(true);

            menu.findItem(R.id.action_add_phone).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.alert_dialog_add_phone, null);
                    builder.setView(dialogView);

                    final EditText phoneNumber = (EditText) dialogView.findViewById(R.id.et_alert_dialog_add_phone);

                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Boolean wantToCloseDialog = false;
                            String parsedPhone = PhoneUtils.parsePhone(phoneNumber.getText().toString(), AppConstants.DEFAULT_COUNTRY_CODE);
                            if (parsedPhone == null)
                            {
                                Toast.makeText(getActivity(), R.string.phone_invalid, Toast.LENGTH_LONG).show();
                                wantToCloseDialog = false;
                            }
                            else
                            {
                                userService.updatePhoneNumber(parsedPhone);

                                menu.findItem(R.id.action_add_phone).setVisible(false);

                                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(dialogView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                wantToCloseDialog = true;

                            }

                            if (wantToCloseDialog)
                            {
                                alertDialog.dismiss();
                            }
                        }
                    });

                    return true;
                }

            });
        }
        else
        {
            menu.findItem(R.id.action_add_phone).setVisible(false);
        }

        menu.findItem(R.id.action_account).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
                return true;
            }
        });

        menu.findItem(R.id.action_create_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                displayCreateEventDialog();
                return true;
            }
        });
    }

    public void displayCreateEventDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View createEventDialogView = inflater.inflate(R.layout.alert_dialog_create_event, null);
        builder.setView(createEventDialogView);

        final EditText eventTitle = (EditText) createEventDialogView.findViewById(R.id.et_dialog_fragment_create_event_title);
        LinearLayout general = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_general);
        LinearLayout eat_out = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_eat_out);
        LinearLayout drinks = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_drinks);
        LinearLayout cafe = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_cafe);
        LinearLayout movie = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_movie);
        LinearLayout outdoors = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_outdoors);
        LinearLayout party = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_party);
        LinearLayout localEvents = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_local_events);
        LinearLayout shopping = (LinearLayout) createEventDialogView.findViewById(R.id.ll_dialog_fragment_create_event_shopping);
        final CheckBox checkBox = (CheckBox) createEventDialogView.findViewById(R.id.cb_dialog_fragment_create_event);

        general.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.GENERAL;
            }
        });

        eat_out.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.EAT_OUT;
            }
        });

        drinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.DRINKS;
            }
        });

        cafe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.CAFE;
            }
        });

        movie.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.MOVIES;
            }
        });

        outdoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.OUTDOORS;
            }
        });

        party.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.PARTY;
            }
        });

        localEvents.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.LOCAL_EVENTS;
            }
        });

        shopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                eventCategory = EventCategory.SHOPPING;
            }
        });

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;

                String title = eventTitle.getText().toString();
                boolean isInviteOnly = checkBox.isChecked();

                if (title == null || title.isEmpty())
                {
                    Toast.makeText(getActivity(), "Please enter a title", Toast.LENGTH_LONG).show();
                    wantToCloseDialog = false;
                }
                else if (eventCategory == null)
                {
                    Toast.makeText(getActivity(), "Please choose a category", Toast.LENGTH_LONG).show();
                    wantToCloseDialog = false;
                }
                else
                {
                    wantToCloseDialog = true;
                    CreateEventFragment createEventFragment = new CreateEventFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.CREATE_EVENT_FRAGMENT_TITLE, title);
                    bundle.putBoolean(BundleKeys.CREATE_EVENT_FRAGMENT_IS_INVITE_ONLY, isInviteOnly);
                    bundle.putSerializable(BundleKeys.CREATE_EVENT_CATEGORY, eventCategory);
                    createEventFragment.setArguments(bundle);
                    FragmentUtils.changeFragment(fragmentManager, createEventFragment);
                }


                if (wantToCloseDialog)
                {
                    dialog.dismiss();
                }

            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btn_home_filter)
        {
            PopupMenu filterMenu = new PopupMenu(getActivity(), filterButton);
            filterMenu.getMenuInflater().inflate(R.menu.popup_filter, filterMenu.getMenu());

            filterMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (menuItem.getItemId() == R.id.menu_filter_today)
                    {
                        filterButton.setText(menuItem.getTitle().toString());
                        filter = Filter.TODAY;
                        refreshRecyclerView();
                    }
                    else if (menuItem.getItemId() == R.id.menu_filter_all)
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
        else if (view.getId() == R.id.btn_home_sort)
        {
            PopupMenu sortMenu = new PopupMenu(getActivity(), sortButton);
            sortMenu.getMenuInflater().inflate(R.menu.popup_sort, sortMenu.getMenu());

            sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (menuItem.getItemId() == R.id.menu_sort_relevance)
                    {
                        sortButton.setText(menuItem.getTitle().toString());
                        sort = Sort.RELEVANCE;
                        refreshRecyclerView();
                    }
                    else if (menuItem.getItemId() == R.id.menu_sort_time)
                    {
                        sortButton.setText(menuItem.getTitle().toString());
                        sort = Sort.DATE_TIME;
                        refreshRecyclerView();
                    }
                    else if (menuItem.getItemId() == R.id.menu_sort_distance)
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
        else if (view.getId() == R.id.ibtn_home_refresh)
        {
            refreshRecyclerView();
        }
        else if (view.getId() == R.id.fib_home_create)
        {
            displayCreateEventDialog();
        }
    }
}
