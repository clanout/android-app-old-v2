package reaper.android.app.ui.screens.create;

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
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.GoogleService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.trigger.event.EventLocationFetchedTrigger;
import reaper.android.app.trigger.event.EventSuggestionsTrigger;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;


/**
 * Created by aditya on 04/07/15.
 */
public class CreateEventFragment extends Fragment implements View.OnClickListener, EventSuggestionsAdapter.EventSuggestionsClickListener, AdapterView.OnItemClickListener
{
    private TextView eventTitle, eventType, timing, noSuggestionsMessage;
    private EditText description;
    private ImageView eventIcon;
    private AutoCompleteTextView location;
    private RecyclerView recyclerView;
    private ImageButton save;

    private String title;
    private Boolean isInviteOnly, isPlaceDetailsRunning, isSaveButtonClicked;
    private EventCategory eventCategory;
    private Event.Type type;
    private Location placeLocation;
    private DateTime startDateTime, endDateTime;

    private FragmentManager manager;
    private Bus bus;

    private EventService eventService;
    private LocationService locationService;
    private GoogleService googleService;

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
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        eventTitle = (TextView) view.findViewById(R.id.tv_create_event_title);
        eventType = (TextView) view.findViewById(R.id.tv_create_event_type);
        timing = (TextView) view.findViewById(R.id.tv_create_event_date_time);
        description = (EditText) view.findViewById(R.id.et_create_event_description);
        noSuggestionsMessage = (TextView) view.findViewById(R.id.tv_create_event_no_suggestions);
        eventIcon = (ImageView) view.findViewById(R.id.iv_create_event_icon);
        location = (AutoCompleteTextView) view.findViewById(R.id.actv_create_event_location);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_create_event_suggestions);
        save = (ImageButton) view.findViewById(R.id.ib_create_event_save);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        title = (String) bundle.get("title");
        isInviteOnly = (Boolean) bundle.get("is_invite_only");
        eventCategory = (EventCategory) bundle.get("category");

        if (eventCategory == null || title == null || title.isEmpty() || isInviteOnly == null)
        {
            throw new IllegalStateException("Event Details cannot be null");

        }

        manager = getActivity().getSupportFragmentManager();
        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        googleService = new GoogleService(bus);
        placeLocation = new Location();

        placeLocation.setZone(locationService.getUserLocation().getZone());

        suggestionList = new ArrayList<>();

        isPlaceDetailsRunning = false;
        isSaveButtonClicked = false;

        save.setOnClickListener(this);
        timing.setOnClickListener(this);

        renderEventDetails(title, isInviteOnly, eventCategory);

        initGoogleAutocompleteAdapter();
        initRecyclerView();

    }

    private void initGoogleAutocompleteAdapter()
    {
        location.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), R.layout.list_item_autocomplete, R.id.tv_list_item_autocomplete, bus));
        location.setOnItemClickListener(this);
    }

    private void initRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recyclerView.setAdapter(eventSuggestionsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recyclerView.setAdapter(eventSuggestionsAdapter);

        if (suggestionList.size() == 0)
        {
            noSuggestionsMessage.setText("No suggestions to show");
            noSuggestionsMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            noSuggestionsMessage.setVisibility(View.GONE);
        }
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
        eventService.fetchEventSuggestions(eventCategory, String.valueOf(userLocation.getLatitude()), String.valueOf(userLocation.getLongitude()));
    }

    private void renderEventDetails(String title, Boolean isInviteOnly, EventCategory eventCategory)
    {
        eventTitle.setText(title);

        if (isInviteOnly)
        {
            eventType.setText("Invite Only");
            type = Event.Type.INVITE_ONLY;
        }
        else
        {
            eventType.setText("Public");
            type = Event.Type.PUBLIC;
        }

        switch (eventCategory)
        {
            case GENERAL:
                eventIcon.setImageResource(R.drawable.ic_event_black_48dp);
                break;
            case EAT_OUT:
                eventIcon.setImageResource(R.drawable.ic_local_restaurant_black_48dp);
                break;
            case DRINKS:
                eventIcon.setImageResource(R.drawable.ic_local_bar_black_48dp);
                break;
            case CAFE:
                eventIcon.setImageResource(R.drawable.ic_local_cafe_black_48dp);
                break;
            case MOVIES:
                eventIcon.setImageResource(R.drawable.ic_local_movies_black_48dp);
                break;
            case OUTDOORS:
                eventIcon.setImageResource(R.drawable.ic_directions_bike_black_48dp);
                break;
            case PARTY:
                eventIcon.setImageResource(R.drawable.ic_location_city_black_48dp);
                break;
            case LOCAL_EVENTS:
                eventIcon.setImageResource(R.drawable.ic_local_attraction_black_48dp);
                break;
            case SHOPPING:
                eventIcon.setImageResource(R.drawable.ic_local_mall_black_48dp);
                break;
            default:
                eventIcon.setImageResource(R.drawable.ic_event_black_48dp);
        }
    }

    @Subscribe
    public void onEventSuggestionsFetched(EventSuggestionsTrigger trigger)
    {
        suggestionList = trigger.getRecommendations();

        refreshRecyclerView();
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
    public void onClick(View v)
    {
        if (v.getId() == R.id.ib_create_event_save)
        {

            if (isPlaceDetailsRunning)
            {
                isSaveButtonClicked = true;
            }
            else
            {
                isSaveButtonClicked = false;
                sendCreateEventRequest();
            }
        }

        if (v.getId() == R.id.tv_create_event_date_time)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View selectTimingsDialogView = inflater.inflate(R.layout.dialog_fragment_select_date_time, null);
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
            timing.setText("Select event timings");
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
                timing.setText(_startDateTime.toString(timeFormatter) + " - " + _endDateTime.toString(timeFormatter) + " (" + _startDateTime.toString(dateFormatter) + ")");
            }
            else
            {
                timing.setText(_startDateTime.toString(timeFormatter) + " (" + _startDateTime.toString(dateFormatter) + ") - " + _endDateTime.toString(timeFormatter) + " (" + _endDateTime.toString(dateFormatter) + ")");
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

    @Override
    public void onEventSuggestionsClicked(View view, int position)
    {
        location.setText(suggestionList.get(position).getName());

        placeLocation.setName(suggestionList.get(position).getName());
        placeLocation.setLatitude(suggestionList.get(position).getLatitude());
        placeLocation.setLongitude(suggestionList.get(position).getLongitude());
    }

    @Subscribe
    public void onEventCreated(EventCreatedTrigger trigger)
    {
        //TODO - Cache update and slect time dialog

        FragmentUtils.changeFragment(manager, new HomeFragment(), false);
    }

    @Subscribe
    public void onEventCreationFailed(GenericErrorTrigger trigger)
    {

        if (trigger.getErrorCode() == ErrorCode.EVENT_CREATION_FAILURE)
        {
            Toast.makeText(getActivity(), "Looks like we messed up! Please try again", Toast.LENGTH_LONG).show();
        }
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
            sendCreateEventRequest();
        }
    }

    private void sendCreateEventRequest()
    {
        String descriptionEvent = description.getText().toString();

        if (descriptionEvent == null)
        {
            descriptionEvent = "";
        }

        eventService.createEvent(title, type, eventCategory, descriptionEvent, placeLocation, startDateTime, endDateTime);

    }

    @Subscribe
    public void onEventLocationNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENT_LOCATION_FETCH_FAILURE)
        {
            Toast.makeText(getActivity(), "Could not find the location. Please try again.", Toast.LENGTH_LONG).show();
            location.setText("");
            placeLocation.setName(null);
            placeLocation.setLatitude(null);
            placeLocation.setLongitude(null);

            isPlaceDetailsRunning = false;

            if (isSaveButtonClicked)
            {
                sendCreateEventRequest();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        GooglePlacesAutocompleteAdapter adapter = (GooglePlacesAutocompleteAdapter) adapterView.getAdapter();
        GooglePlaceAutocompleteApiResponse.Prediction prediction = adapter.getItem(i);

        googleService.getPlaceDetails(prediction.getPlaceId());
    }
}
