package reaper.android.app.ui.screens.create;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventCreatedTrigger;
import reaper.android.app.trigger.event.EventSuggestionsTrigger;
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

    private String title, locationName, locationLatitude, locationLongitude, locationZone;
    private Boolean isInviteOnly;
    private EventCategory eventCategory;
    private Event.Type type;

    private FragmentManager manager;
    private Bus bus;

    private EventService eventService;
    private LocationService locationService;

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

        locationZone = locationService.getUserLocation().getZone();

        suggestionList = new ArrayList<>();

        save.setOnClickListener(this);

        renderEventDetails(title, isInviteOnly, eventCategory);

        initGoogleAutocompleteAdapter();
        initRecyclerView();

    }

    private void initGoogleAutocompleteAdapter()
    {
        location.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), R.layout.list_item_autocomplete, R.id.tv_list_item_autocomplete , bus, "food"));
        location.setOnItemClickListener(this);
    }

    private void initRecyclerView()
    {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recyclerView.setAdapter(eventSuggestionsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecyclerView() {
        eventSuggestionsAdapter = new EventSuggestionsAdapter(getActivity(), suggestionList);
        eventSuggestionsAdapter.setEventSuggestionsClickListener(this);

        recyclerView.setAdapter(eventSuggestionsAdapter);

        if (suggestionList.size() == 0) {
            noSuggestionsMessage.setText("No suggestions to show");
            noSuggestionsMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {
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

        if(isInviteOnly){
            eventType.setText("Invite Only");
            type = Event.Type.INVITE_ONLY;
        }else{
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
    public void onEventSuggestionsFetched(EventSuggestionsTrigger trigger){
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
        if(v.getId() == R.id.ib_create_event_save){

            String descriptionEvent = description.getText().toString();
            eventService.createEvent(title, type, eventCategory, descriptionEvent, locationName, locationZone, locationLatitude, locationLongitude, DateTime.now(), DateTime.now());
        }
    }

    @Override
    public void onEventSuggestionsClicked(View view, int position)
    {
        location.setText(suggestionList.get(position).getName());

        locationName = suggestionList.get(position).getName();
        locationLatitude = suggestionList.get(position).getLatitude();
        locationLongitude = suggestionList.get(position).getLongitude();
    }

    @Subscribe
    public void onEventCreated(EventCreatedTrigger trigger){
        Toast.makeText(getActivity(), "Event: " + trigger.getEventId(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onEventCreationFailed(GenericErrorTrigger trigger){

        if(trigger.getErrorCode() == ErrorCode.EVENT_CREATION_FAILURE){
            Toast.makeText(getActivity(), "Looks like we messed up! Please try again", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {

    }
}
