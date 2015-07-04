package reaper.android.app.ui.screens.edit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.EventUtils;
import reaper.android.common.communicator.Communicator;

public class EditEventFragment extends Fragment
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private EventService eventService;
    private LocationService locationService;
    private UserService userService;

    // Data
    private Event event;
    private EventDetails eventDetails;

    // UI Elements
    private TextView title;
    private TextView type;
    private ImageView icon;
    private AutoCompleteTextView eventLocation;
    private TextView schedule;
    private EditText description;
    private RecyclerView recommendationList;
    private TextView noSuggestions;

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
        schedule = (TextView) view.findViewById(R.id.tv_create_event_date_time);
        description = (EditText) view.findViewById(R.id.et_edit_event_description);
        recommendationList = (RecyclerView) view.findViewById(R.id.rv_edit_event_suggestions);
        noSuggestions = (TextView) view.findViewById(R.id.tv_edit_event_no_suggestions);

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

        render();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_button, menu);

        menu.findItem(R.id.action_account).setVisible(false);
        menu.findItem(R.id.action_create_event).setVisible(false);
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(true);

        if (EventUtils.canDeleteEvent(event, userService.getActiveUser()))
        {
            menu.findItem(R.id.action_delete_event).setVisible(true);
            menu.findItem(R.id.action_delete_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    Toast.makeText(getActivity(), "Delete Event (" + event.getTitle() + ")", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        else
        {
            menu.findItem(R.id.action_delete_event).setVisible(false);
        }

        if (EventUtils.canFinaliseEvent(event, userService.getActiveUser()))
        {
            menu.findItem(R.id.action_finalize_event).setVisible(true);
            menu.findItem(R.id.action_finalize_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    Toast.makeText(getActivity(), "Finalize Event (" + event.getTitle() + ")", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        else
        {
            menu.findItem(R.id.action_finalize_event).setVisible(false);
        }
    }

}
