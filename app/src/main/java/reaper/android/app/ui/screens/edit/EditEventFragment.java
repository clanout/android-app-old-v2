package reaper.android.app.ui.screens.edit;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import reaper.android.R;
import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.GoogleService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventEditedTrigger;
import reaper.android.app.trigger.event.EventLocationFetchedTrigger;
import reaper.android.app.trigger.event.EventSuggestionsTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.ui.screens.create.EventSuggestionsAdapter;
import reaper.android.app.ui.screens.create.GooglePlacesAutocompleteAdapter;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.EventUtils;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;


public class EditEventFragment extends Fragment implements AdapterView.OnItemClickListener, EventSuggestionsAdapter.EventSuggestionsClickListener, View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private EventService eventService;
    private LocationService locationService;
    private UserService userService;
    private GoogleService googleService;

    // Data
    private Event event;
    private String eventId;
    private EventDetails eventDetails;
    private Location placeLocation;
    private DateTime startDateTime, endDateTime;

    // UI Elements
    private TextView title;
    private TextView type;
    private ImageView icon;
    private AutoCompleteTextView eventLocation;
    private TextView schedule;
    private EditText description;
    private RecyclerView recommendationList;
    private TextView noSuggestions;
    private ImageButton save;

    private boolean isPlaceDetailsRunning, isSaveButtonClicked, isFinalised;

    private List<Suggestion> suggestionList;
    private EventSuggestionsAdapter eventSuggestionsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        title = (TextView) view.findViewById(R.id.tv_edit_event_title);
        type = (TextView) view.findViewById(R.id.tv_edit_event_type);
        icon = (ImageView) view.findViewById(R.id.iv_edit_event_icon);
        eventLocation = (AutoCompleteTextView) view.findViewById(R.id.actv_edit_event_location);
        schedule = (TextView) view.findViewById(R.id.tv_edit_event_date_time);
        description = (EditText) view.findViewById(R.id.et_edit_event_description);
        recommendationList = (RecyclerView) view.findViewById(R.id.rv_edit_event_suggestions);
        noSuggestions = (TextView) view.findViewById(R.id.tv_edit_event_no_suggestions);
        save = (ImageButton) view.findViewById(R.id.ib_edit_event_save);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        event = (Event) bundle.get("event");
        eventDetails = (EventDetails) bundle.get("event_details");

        if (event == null || eventDetails == null)
        {
            throw new IllegalStateException("Event/EventDetails cannot be null while creating EditEventFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        userService = new UserService(bus);
        googleService = new GoogleService(bus);

        placeLocation = event.getLocation();

        suggestionList = new ArrayList<>();

        isPlaceDetailsRunning = false;
        isSaveButtonClicked = false;
        isFinalised = event.isFinalized();

        schedule.setOnClickListener(this);
        save.setOnClickListener(this);

        render();
        initGoogleAutocompleteAdapter();
        initRecyclerView();
    }

    private void render()
    {
        EventCategory category = EventCategory.valueOf(event.getCategory());
        switch (category)
        {
            case GENERAL:
                icon.setImageResource(R.drawable.ic_event_black_48dp);
                break;
            case EAT_OUT:
                icon.setImageResource(R.drawable.ic_local_restaurant_black_48dp);
                break;
            case DRINKS:
                icon.setImageResource(R.drawable.ic_local_bar_black_48dp);
                break;
            case CAFE:
                icon.setImageResource(R.drawable.ic_local_cafe_black_48dp);
                break;
            case MOVIES:
                icon.setImageResource(R.drawable.ic_local_movies_black_48dp);
                break;
            case OUTDOORS:
                icon.setImageResource(R.drawable.ic_directions_bike_black_48dp);
                break;
            case PARTY:
                icon.setImageResource(R.drawable.ic_location_city_black_48dp);
                break;
            case LOCAL_EVENTS:
                icon.setImageResource(R.drawable.ic_local_attraction_black_48dp);
                break;
            case SHOPPING:
                icon.setImageResource(R.drawable.ic_local_mall_black_48dp);
                break;
            default:
                icon.setImageResource(R.drawable.ic_event_black_48dp);
        }


        title.setText(event.getTitle());

        if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
        {
            eventLocation.setText(R.string.event_details_no_location);
        }
        else
        {
            eventLocation.setText(event.getLocation().getName());
        }

        DateTime start = event.getStartTime();
        DateTime end = event.getEndTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        if (start.toString(dateFormatter).equals(end.toString(dateFormatter)))
        {
            schedule.setText(start.toString(timeFormatter) + " - " + end.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ")");
        }
        else
        {
            schedule.setText(start.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ") - " + end.toString(timeFormatter) + " (" + end.toString(dateFormatter) + ")");
        }

        if (event.getType() == Event.Type.PUBLIC)
        {
            type.setText(R.string.event_details_type_public);
        }
        else
        {
            type.setText(R.string.event_details_type_invite_only);
        }

        description.setText(eventDetails.getDescription());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        bus.unregister(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);

        Location userLocation = locationService.getUserLocation();
        eventService.fetchEventSuggestions(EventCategory.valueOf(event.getCategory()), String.valueOf(userLocation.getLatitude()), String.valueOf(userLocation.getLongitude()));
    }

    private void initGoogleAutocompleteAdapter()
    {
        eventLocation.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), R.layout.list_item_autocomplete, R.id.tv_list_item_autocomplete, bus));
        eventLocation.setOnItemClickListener(this);
    }

    private void initRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recommendationList.setAdapter(eventSuggestionsAdapter);
        recommendationList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recommendationList.setAdapter(eventSuggestionsAdapter);

        if (suggestionList.size() == 0)
        {
            noSuggestions.setText("No suggestions to show");
            noSuggestions.setVisibility(View.VISIBLE);
            recommendationList.setVisibility(View.GONE);

        }
        else
        {
            recommendationList.setVisibility(View.VISIBLE);
            noSuggestions.setVisibility(View.GONE);
        }
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);

        if (EventUtils.canDeleteEvent(event, userService.getActiveUserId()))
        {
            menu.findItem(R.id.action_delete_event).setVisible(true);
            menu.findItem(R.id.action_delete_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setMessage("Are you sure you want to delete this event?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            eventService.deleteEvent(event);
                            eventService.deleteCacheFor(event);
                            Toast.makeText(getActivity(), "The event has been deleted", Toast.LENGTH_SHORT).show();
                            FragmentUtils.changeFragment(fragmentManager, new HomeFragment(), false);
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    return true;
                }
            });
        }
        else
        {
            menu.findItem(R.id.action_delete_event).setVisible(false);
        }

        if (EventUtils.canFinaliseEvent(event, userService.getActiveUserId()))
        {
            if (isFinalised)
            {
                menu.findItem(R.id.action_finalize_event).setIcon(R.drawable.ic_action_secure);
                menu.findItem(R.id.action_finalize_event).setVisible(true);
            }
            else
            {
                menu.findItem(R.id.action_finalize_event).setIcon(R.drawable.ic_action_error);
                menu.findItem(R.id.action_finalize_event).setVisible(true);
            }

            menu.findItem(R.id.action_finalize_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {

                    if (isFinalised)
                    {
                        isFinalised = false;
                        menu.findItem(R.id.action_finalize_event).setIcon(R.drawable.ic_action_error);
                        Toast.makeText(getActivity(), "The event is now not finalised. People going to the event can update it.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        menu.findItem(R.id.action_finalize_event).setIcon(R.drawable.ic_action_secure);
                        isFinalised = true;
                        Toast.makeText(getActivity(), "The event is now finalised. No one can make any changes to it.", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });
        }
        else
        {
            menu.findItem(R.id.action_finalize_event).setVisible(false);
        }
    }

    @Subscribe
    public void onEventSuggestionsFetched(EventSuggestionsTrigger trigger)
    {
        suggestionList = trigger.getRecommendations();

        refreshRecyclerView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        GooglePlacesAutocompleteAdapter adapter = (GooglePlacesAutocompleteAdapter) parent.getAdapter();
        GooglePlaceAutocompleteApiResponse.Prediction prediction = adapter.getItem(position);

        googleService.getPlaceDetails(prediction.getPlaceId());
    }

    @Override
    public void onEventSuggestionsClicked(View view, int position)
    {
        eventLocation.setText(suggestionList.get(position).getName());

        placeLocation.setName(suggestionList.get(position).getName());
        placeLocation.setLatitude(suggestionList.get(position).getLatitude());
        placeLocation.setLongitude(suggestionList.get(position).getLongitude());

    }

    @Subscribe
    public void onEventLocationFetched(EventLocationFetchedTrigger trigger)
    {
        placeLocation.setName(trigger.getLocation().getName());
        placeLocation.setLatitude(trigger.getLocation().getLatitude());
        placeLocation.setLongitude(trigger.getLocation().getLongitude());

        isPlaceDetailsRunning = false;

        if (isSaveButtonClicked)
        {
            sendEditEventRequest();
        }
    }

    @Subscribe
    public void onEventLocationNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENT_LOCATION_FETCH_FAILURE)
        {
            Toast.makeText(getActivity(), "Could not find the location. Please try again.", Toast.LENGTH_LONG).show();
            eventLocation.setText("");
            placeLocation.setName(null);
            placeLocation.setLatitude(null);
            placeLocation.setLongitude(null);

            isPlaceDetailsRunning = false;

            if (isSaveButtonClicked)
            {
                sendEditEventRequest();
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.ib_edit_event_save)
        {

            if (isPlaceDetailsRunning)
            {
                isSaveButtonClicked = true;
            }
            else
            {
                isSaveButtonClicked = false;
                sendEditEventRequest();
            }
        }

        if(v.getId() == R.id.tv_edit_event_date_time)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View selectTimingsDialogView = inflater.inflate(R.layout.alert_dialog_select_date_time, null);
            builder.setView(selectTimingsDialogView);

            final TimePicker startTimePicker = (TimePicker) selectTimingsDialogView.findViewById(R.id.tp_select_time_start);
            final TimePicker endTimePicker = (TimePicker) selectTimingsDialogView.findViewById(R.id.tp_select_time_end);
            final DatePicker startDatePicker = (DatePicker) selectTimingsDialogView.findViewById(R.id.dp_select_date_start);
            final DatePicker endDatePicker = (DatePicker) selectTimingsDialogView.findViewById(R.id.dp_select_date_end);

            renderDateAndTimePickers(startDatePicker, endDatePicker, startTimePicker, endTimePicker);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startDateTime = getTime(startDatePicker, startTimePicker);
                    endDateTime = getTime(endDatePicker, endTimePicker);

                    renderEventTimings(startDateTime, endDateTime);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }


    }

    private void renderEventTimings(DateTime _startDateTime, DateTime _endDateTime)
    {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        if (_startDateTime.isAfter(_endDateTime))
        {
            schedule.setText("Select event timings");
            startDateTime = null;
            endDateTime = null;
            Toast.makeText(getActivity(), "An event can't end before it has even started", Toast.LENGTH_LONG).show();
        }
        else
        {
            startDateTime = _startDateTime;
            endDateTime = _endDateTime;

            if (_startDateTime.toString(dateFormatter).equals(_endDateTime.toString(dateFormatter)))
            {
                schedule.setText(_startDateTime.toString(timeFormatter) + " - " + _endDateTime.toString(timeFormatter) + " (" + _startDateTime.toString(dateFormatter) + ")");
            }
            else
            {
                schedule.setText(_startDateTime.toString(timeFormatter) + " (" + _startDateTime.toString(dateFormatter) + ") - " + _endDateTime.toString(timeFormatter) + " (" + _endDateTime.toString(dateFormatter) + ")");
            }
        }
    }

    private DateTime getTime(DatePicker datePicker, TimePicker timePicker)
    {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int date = datePicker.getDayOfMonth();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        String timeZone = DateTime.now().toString(DateTimeFormat.forPattern("Z"));

        StringBuilder formattedDate = new StringBuilder();
        formattedDate.append(year);
        formattedDate.append("-");
        formattedDate.append(String.format("%02d", month));
        formattedDate.append("-");
        formattedDate.append(String.format("%02d", date));
        formattedDate.append("T");
        formattedDate.append(String.format("%02d", hour));
        formattedDate.append(":");
        formattedDate.append(String.format("%02d", minute));
        formattedDate.append(":00.000");
        formattedDate.append(timeZone);
        DateTime dateTime = DateTime.parse(formattedDate.toString());

        return dateTime;
    }

    private void renderDateAndTimePickers(DatePicker startDatePicker, DatePicker endDatePicker, TimePicker startTimePicker, TimePicker endTimePicker)
    {
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        hideYearinDatePicker(startDatePicker);
        hideYearinDatePicker(endDatePicker);
        startDatePicker.setMinDate(System.currentTimeMillis() - 1000);
        endDatePicker.setMinDate(System.currentTimeMillis() - 1000);

        if (startDateTime == null || endDateTime == null)
        {
            startTimePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            endTimePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        }
        else
        {
            startTimePicker.setCurrentHour(startDateTime.getHourOfDay());
            startTimePicker.setCurrentMinute(startDateTime.getMinuteOfHour());

            endTimePicker.setCurrentHour(endDateTime.getHourOfDay());
            endTimePicker.setCurrentMinute(endDateTime.getMinuteOfHour());

            startDatePicker.updateDate(startDateTime.getYear(), startDateTime.getMonthOfYear() - 1, startDateTime.getDayOfMonth());
            endDatePicker.updateDate(endDateTime.getYear(), endDateTime.getMonthOfYear() - 1, endDateTime.getDayOfMonth());
        }
    }

    private void hideYearinDatePicker(DatePicker datePicker)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int yearSpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
            if (yearSpinnerId != 0)
            {
                View yearSpinner = datePicker.findViewById(yearSpinnerId);
                if (yearSpinner != null)
                {
                    yearSpinner.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            try
            {
                Field f[] = datePicker.getClass().getDeclaredFields();
                for (Field field : f)
                {
                    if (field.getName().equals("mYearPicker"))
                    {
                        field.setAccessible(true);
                        Object yearPicker = new Object();
                        yearPicker = field.get(datePicker);
                        ((View) yearPicker).setVisibility(View.GONE);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendEditEventRequest()
    {
        String descriptionEvent = description.getText().toString();

        if (descriptionEvent == null)
        {
            descriptionEvent = "";
        }

        eventService.editEvent(event.getId(), isFinalised, startDateTime, endDateTime, placeLocation, descriptionEvent);
    }

    @Subscribe
    public void onEventEdited(EventEditedTrigger trigger)
    {
        eventService.updateCacheFor(trigger.getEvent());
        eventId = trigger.getEvent().getId();

        eventService.fetchEvents(locationService.getUserLocation().getZone());
    }

    @Subscribe
    public void onEventsFetchedEdit(EventsFetchTrigger trigger)
    {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(eventId);

        int activePosition = events.indexOf(activeEvent);

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("events", (ArrayList<Event>) events);
        bundle.putInt("active_event", activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment, false);

    }

    @Subscribe
    public void onEventEditFailed(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENT_EDIT_FAILURE)
        {
            Toast.makeText(getActivity(), "Looks like we messed up! Please try again", Toast.LENGTH_LONG).show();
        }
    }
}
