package reaper.android.app.ui.screens.details;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.ui.screens.edit.EditEventFragment;
import reaper.android.app.ui.util.EventUtils;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class EventDetailsFragment extends Fragment implements View.OnClickListener
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private UserService userService;
    private EventService eventService;

    // Data
    private Event event;
    private EventDetails eventDetails;

    // UI Elements
    private ImageView icon, locationIcon;
    private TextView title, type, description, location, dateTime;
    private RecyclerView attendeeList;
    private TextView noAttendeeMessage;

    private EventAttendeesAdapter eventAttendeesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        icon = (ImageView) view.findViewById(R.id.iv_event_details_icon);
        locationIcon = (ImageView) view.findViewById(R.id.iv_event_details_location);
        title = (TextView) view.findViewById(R.id.tv_event_details_title);
        type = (TextView) view.findViewById(R.id.tv_event_details_type);
        description = (TextView) view.findViewById(R.id.tv_event_details_description);
        location = (TextView) view.findViewById(R.id.tv_event_details_location);
        dateTime = (TextView) view.findViewById(R.id.tv_event_details_date_time);
        attendeeList = (RecyclerView) view.findViewById(R.id.rv_event_details_attendees);
        noAttendeeMessage = (TextView) view.findViewById(R.id.tv_event_details_no_attendees);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        event = (Event) bundle.get("event");

        if (event == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);

        location.setOnClickListener(this);
        description.setOnClickListener(this);
        dateTime.setOnClickListener(this);

        renderEventSummary();

        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        bus.register(this);
        eventService.fetchEventDetails(event.getId());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onEventDetailsFetchTrigger(EventDetailsFetchTrigger trigger)
    {
        if (trigger.getEventDetails().getId().equals(event.getId()))
        {
            eventDetails = trigger.getEventDetails();

            if (eventDetails.getDescription() == null || eventDetails.getDescription().isEmpty())
            {
                description.setText(R.string.event_details_no_description);
            }
            else
            {
                description.setText(eventDetails.getDescription());
            }

            refreshRecyclerView();
        }
    }

    private void initRecyclerView()
    {
        attendeeList.setLayoutManager(new LinearLayoutManager(getActivity()));

        eventAttendeesAdapter = new EventAttendeesAdapter(new ArrayList<EventDetails.Attendee>());
        attendeeList.setAdapter(eventAttendeesAdapter);
    }

    private void refreshRecyclerView()
    {
        if (eventDetails == null)
        {
            return;
        }

        if (eventDetails.getAttendees().size() == 0)
        {
            setNoAttendeeView();
        }
        else
        {
            setNormalView();
        }

        eventAttendeesAdapter = new EventAttendeesAdapter(eventDetails.getAttendees());
        attendeeList.setAdapter(eventAttendeesAdapter);
    }

    private void setNormalView()
    {
        attendeeList.setVisibility(View.VISIBLE);
        noAttendeeMessage.setVisibility(View.GONE);
    }

    private void setNoAttendeeView()
    {
        noAttendeeMessage.setText(R.string.event_details_no_attendees);
        noAttendeeMessage.setVisibility(View.VISIBLE);
        attendeeList.setVisibility(View.GONE);
    }

    private void renderEventSummary()
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
            location.setText(R.string.event_details_no_location);
            location.setOnClickListener(null);
        }
        else
        {
            location.setText(event.getLocation().getName());
//            location.setOnClickListener(this);
        }

        DateTime start = event.getStartTime();
        DateTime end = event.getEndTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        if (start.toString(dateFormatter).equals(end.toString(dateFormatter)))
        {
            dateTime.setText(start.toString(timeFormatter) + " - " + end.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ")");
        }
        else
        {
            dateTime.setText(start.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ") - " + end.toString(timeFormatter) + " (" + end.toString(dateFormatter) + ")");
        }

        if (event.getType() == Event.Type.PUBLIC)
        {
            type.setText(R.string.event_details_type_public);
        }
        else
        {
            type.setText(R.string.event_details_type_invite_only);
        }
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

        if (EventUtils.canEdit(event, userService.getActiveUser()))
        {
            menu.findItem(R.id.action_edit_event).setVisible(true);
            menu.findItem(R.id.action_edit_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    EditEventFragment editEventFragment = new EditEventFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    bundle.putSerializable("event_details", eventDetails);
                    editEventFragment.setArguments(bundle);
                    FragmentUtils.changeFragment(fragmentManager, editEventFragment, true);
                    return true;
                }
            });
        }
        else
        {
            menu.findItem(R.id.action_edit_event).setVisible(false);
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.tv_event_details_location)
        {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + event.getLocation().getLatitude() + "," + event.getLocation().getLongitude()));
            startActivity(intent);
        }
        else if (view.getId() == R.id.tv_event_details_description)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Description")
                    .setMessage(eventDetails.getDescription())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create().show();
        }
        else if (view.getId() == R.id.tv_event_details_date_time)
        {
            Snackbar.make(this.getView(), "Set Reminder", Snackbar.LENGTH_LONG).setAction("OK", null).show();
        }
    }
}
