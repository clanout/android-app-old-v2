package reaper.android.app.ui.screens.details;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.ChangeAttendeeListTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventRsvpNotChangedTrigger;
import reaper.android.app.ui.screens.edit.EditEventFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.app.ui.util.event.EventUtilsConstants;
import reaper.android.common.communicator.Communicator;

public class EventDetailsFragment extends Fragment implements View.OnClickListener, AttendeeClickCommunicator
{
    private FragmentManager fragmentManager;
    private Bus bus;

    // Services
    private UserService userService;
    private EventService eventService;
    private EventCache eventCache;

    // Data
    private Event event;
    private EventDetails eventDetails;

    // UI Elements
    private ImageView icon, locationIcon;
    private TextView title, type, description, location, dateTime;
    private RecyclerView attendeeList;
    private TextView noAttendeeMessage;

    private EventAttendeesAdapter eventAttendeesAdapter;

    private boolean areEventDetailsFetched;

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
        event = (Event) bundle.get(BundleKeys.EVENT_DETAILS_FRAGMENT_EVENT);

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

        eventCache = CacheManager.getEventCache();

        renderEventSummary();
        initRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        areEventDetailsFetched = false;
        bus.register(this);
        eventCache.markSeen(event.getId());
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
            } else
            {
                description.setText(eventDetails.getDescription());
            }

            refreshRecyclerView();

            areEventDetailsFetched = true;
        }
    }

    private void initRecyclerView()
    {
        attendeeList.setLayoutManager(new LinearLayoutManager(getActivity()));

        eventAttendeesAdapter = new EventAttendeesAdapter(new ArrayList<EventDetails.Attendee>());
        eventAttendeesAdapter.setAttendeeClickCommunicator(this);
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
        } else
        {
            setNormalView();
        }

        EventDetails.Attendee attendee = new EventDetails.Attendee();
        attendee.setId(userService.getActiveUserId());

        if (eventDetails.getAttendees().contains(attendee))
        {
            eventDetails.getAttendees().remove(attendee);
        }

        attendee.setName(userService.getActiveUserName());
        attendee.setFriend(false);
        attendee.setInviter(false);
        attendee.setRsvp(event.getRsvp());

        if (!(event.getRsvp() == Event.RSVP.NO))
        {
            eventDetails.getAttendees().add(attendee);
        }

        eventAttendeesAdapter = new EventAttendeesAdapter(eventDetails.getAttendees());
        eventAttendeesAdapter.setAttendeeClickCommunicator(this);
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
        } else
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
        } else
        {
            dateTime.setText(start.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ") - " + end.toString(timeFormatter) + " (" + end.toString(dateFormatter) + ")");
        }

        if (event.getType() == Event.Type.PUBLIC)
        {
            type.setText(R.string.event_details_type_public);
        } else
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
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(true);

        menu.findItem(R.id.action_edit_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (EventUtils.canEdit(event, userService.getActiveUserId()) == EventUtilsConstants.CAN_EDIT)
                {
                    if (areEventDetailsFetched)
                    {
                        EditEventFragment editEventFragment = new EditEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT, event);
                        bundle.putSerializable(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT_DETAILS, eventDetails);
                        editEventFragment.setArguments(bundle);
                        FragmentUtils.changeFragment(fragmentManager, editEventFragment);
                    }
                } else if (EventUtils.canEdit(event, userService.getActiveUserId()) == EventUtilsConstants.CANNOT_EDIT_LOCKED)
                {
                    Toast.makeText(getActivity(), R.string.cannot_edit_event_locked, Toast.LENGTH_LONG).show();

                } else if (EventUtils.canEdit(event, userService.getActiveUserId()) == EventUtilsConstants.CANNOT_EDIT_NOT_GOING)
                {
                    Toast.makeText(getActivity(), R.string.cannot_edit_event_not_going, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.tv_event_details_location)
        {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + event.getLocation().getLatitude() + "," + event.getLocation().getLongitude()));
            startActivity(intent);
        } else if (view.getId() == R.id.tv_event_details_description)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Base_Theme_AppCompat_Light_Dialog_Alert);

            if (description.getText().toString().isEmpty())
            {
                builder.setMessage(description.getText().toString());
            } else
            {
                builder.setMessage(description.getText().toString());
            }

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        } else if (view.getId() == R.id.tv_event_details_date_time)
        {
        }
    }

    @Subscribe
    public void onRsvpChanged(ChangeAttendeeListTrigger trigger)
    {
        if (event.getId().equals(trigger.getEventId()))
        {
            EventDetails.Attendee attendee = new EventDetails.Attendee();
            attendee.setId(userService.getActiveUserId());

            if (trigger.getRsvp() == Event.RSVP.YES)
            {
                if (eventDetails != null)
                {
                    event.setRsvp(Event.RSVP.YES);

                    if (eventDetails.getAttendees().contains(attendee))
                    {
                        eventDetails.getAttendees().remove(attendee);
                    }

                    attendee.setRsvp(Event.RSVP.YES);
                    attendee.setName(userService.getActiveUserName());
                    attendee.setFriend(false);
                    attendee.setInviter(false);

                    eventDetails.getAttendees().add(attendee);
                    refreshRecyclerView();
                }
            } else if (trigger.getRsvp() == Event.RSVP.MAYBE)
            {
                if (eventDetails != null)
                {
                    event.setRsvp(Event.RSVP.MAYBE);

                    if (eventDetails.getAttendees().contains(attendee))
                    {
                        eventDetails.getAttendees().remove(attendee);
                    }

                    attendee.setRsvp(Event.RSVP.MAYBE);
                    attendee.setName(userService.getActiveUserName());
                    attendee.setFriend(false);
                    attendee.setInviter(false);

                    eventDetails.getAttendees().add(attendee);
                    refreshRecyclerView();
                }

            } else if (trigger.getRsvp() == Event.RSVP.NO)
            {
                if (eventDetails != null)
                {
                    event.setRsvp(Event.RSVP.NO);

                    attendee.setRsvp(Event.RSVP.NO);
                    if (eventDetails.getAttendees().contains(attendee))
                    {
                        eventDetails.getAttendees().remove(attendee);
                        refreshRecyclerView();
                    }
                }
            }
        }
    }

    @Subscribe
    public void onRsvpNotChanged(EventRsvpNotChangedTrigger trigger)
    {
        if (trigger.getEventId().equals(event.getId()))
        {
            event.setRsvp(trigger.getOldRsvp());
        }
    }

    @Override
    public void onAttendeeClicked(String name)
    {
        Toast.makeText(getActivity(), name + " has invited you to this event", Toast.LENGTH_LONG).show();
    }
}
