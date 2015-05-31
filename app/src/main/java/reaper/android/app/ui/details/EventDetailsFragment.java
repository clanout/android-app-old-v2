package reaper.android.app.ui.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.EventDetailsFetchTrigger;
import reaper.android.common.communicator.Communicator;

public class EventDetailsFragment extends Fragment
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
    private TextView title, description, location, startDateTime, endDateTime;
    private Button rsvp, invite, chat;
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
        description = (TextView) view.findViewById(R.id.tv_event_details_description);
        location = (TextView) view.findViewById(R.id.tv_event_details_location);
        startDateTime = (TextView) view.findViewById(R.id.tv_event_eetails_start_date_time);
        endDateTime = (TextView) view.findViewById(R.id.tv_event_details_end_date_time);
        rsvp = (Button) view.findViewById(R.id.btn_event_details_rsvp);
        invite = (Button) view.findViewById(R.id.btn_event_details_invite);
        chat = (Button) view.findViewById(R.id.btn_event_details_chat);
        attendeeList = (RecyclerView) view.findViewById(R.id.rv_event_details_attendees);
        noAttendeeMessage = (TextView) view.findViewById(R.id.tv_event_details_no_attendees);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            event = (Event) savedInstanceState.get("event");
        }
        else
        {
            Bundle bundle = getArguments();
            event = (Event) bundle.get("event");
        }

        if (event == null)
        {
            throw new IllegalStateException("Event cannot be null while creating EventDetailsFragment instance");
        }

        bus = Communicator.getInstance().getBus();
        fragmentManager = getActivity().getSupportFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);

        if (event.getRsvp() == Event.RSVP.YES)
        {
            rsvp.setText("Going");
        }
        else if (event.getRsvp() == Event.RSVP.NO)
        {
            rsvp.setText("Not Going");
        }
        else if (event.getRsvp() == Event.RSVP.MAYBE)
        {
            rsvp.setText("Maybe");
        }

//        invite.setOnClickListener(this);
//        rsvp.setOnClickListener(this);
//        chat.setOnClickListener(this);
//        location.setOnClickListener(this);

        renderEventSummary();

        initRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("event", event);
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
        eventDetails = trigger.getEventDetails();

        if (eventDetails.getDescription() == null || eventDetails.getDescription().isEmpty())
        {
            description.setText("No Description");
        }
        else
        {
            description.setText(eventDetails.getDescription());
        }

        refreshRecyclerView();
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
        noAttendeeMessage.setText("No users to show");
        noAttendeeMessage.setVisibility(View.VISIBLE);
        attendeeList.setVisibility(View.GONE);
    }

    private void renderEventSummary()
    {
        icon.setImageResource(R.drawable.ic_local_bar_black_48dp);

        title.setText(event.getTitle());

        if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
        {
            location.setText("Location Not Specified");
            locationIcon.setVisibility(View.INVISIBLE);
            location.setOnClickListener(null);
        }
        else
        {
            location.setText(event.getLocation().getName());
            locationIcon.setVisibility(View.VISIBLE);
//            location.setOnClickListener(this);
        }

        DateTime start = event.getStartTime();
        DateTime end = event.getEndTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM dd");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        startDateTime.setText(start.toString(timeFormatter) + ", " + start.toString(dateFormatter));
        endDateTime.setText(end.toString(timeFormatter) + ", " + end.toString(dateFormatter));
    }
}
