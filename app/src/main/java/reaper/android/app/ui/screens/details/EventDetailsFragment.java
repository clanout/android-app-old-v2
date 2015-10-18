package reaper.android.app.ui.screens.details;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventAttendeeComparator;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.event.ChangeAttendeeListTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchedFromNetworkTrigger;
import reaper.android.app.trigger.event.EventRsvpNotChangedTrigger;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.util.DrawableFactory;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.app.ui.util.event.EventUtilsConstants;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class EventDetailsFragment extends BaseFragment implements View.OnClickListener, AttendeeClickCommunicator
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
    private MaterialIconView icon;
    private LinearLayout iconContainer;
    private TextView title, type, description, location, dateTime;
    private RecyclerView attendeeList;
    private TextView noAttendeeMessage, refreshDetailsTextView;
    private ProgressBar refreshDetailsProgressBar;
    private Drawable pencilDrawable;

    private EventAttendeesAdapter eventAttendeesAdapter;

    private boolean areEventDetailsFetched;
    private List<EventDetails.Attendee> attendees;

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

        icon = (MaterialIconView) view.findViewById(R.id.miv_event_details_icon);
        title = (TextView) view.findViewById(R.id.tv_event_details_title);
        type = (TextView) view.findViewById(R.id.tv_event_details_type);
        description = (TextView) view.findViewById(R.id.tv_event_details_description);
        location = (TextView) view.findViewById(R.id.tv_event_details_location);
        dateTime = (TextView) view.findViewById(R.id.tv_event_details_date_time);
        attendeeList = (RecyclerView) view.findViewById(R.id.rv_event_details_attendees);
        noAttendeeMessage = (TextView) view.findViewById(R.id.tv_event_details_no_attendees);
        refreshDetailsProgressBar = (ProgressBar) view.findViewById(R.id.pb_event_details_refresh_details);
        refreshDetailsTextView = (TextView) view.findViewById(R.id.tv_event_details_refresh_details);
        iconContainer = (LinearLayout) view.findViewById(R.id.ll_event_details_icon_container);

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
        fragmentManager = getActivity().getFragmentManager();
        userService = new UserService(bus);
        eventService = new EventService(bus);

        location.setOnClickListener(this);
        description.setOnClickListener(this);
        dateTime.setOnClickListener(this);

        eventCache = CacheManager.getEventCache();

        generateDrawables();

        renderEventSummary();
        initRecyclerView();
    }

    private void generateDrawables()
    {
        pencilDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                .setColor(ContextCompat.getColor(getActivity(), R.color.whity))
                .setSizeDp(36)
                .build();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.EVENT_DETAILS_FRAGMENT);

        areEventDetailsFetched = false;
        bus.register(this);
        eventCache.markSeen(event.getId());
        eventService.fetchEventDetails(event.getId());

        eventService.fetchEventDetailsFromNetwork(event.getId());
        refreshDetailsProgressBar.setVisibility(View.VISIBLE);
        refreshDetailsTextView.setVisibility(View.VISIBLE);
        refreshDetailsTextView.setText(R.string.refreshing_attendee_list);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onEventDetailsFetchedFromNetwork(EventDetailsFetchedFromNetworkTrigger trigger)
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

            refreshDetailsProgressBar.setVisibility(View.GONE);
            refreshDetailsTextView.setVisibility(View.GONE);

            int numberOfFriendsGoing = 0;
            List<EventDetails.Attendee> attendeeList = trigger.getEventDetails().getAttendees();
            for (EventDetails.Attendee attendee : attendeeList)
            {
                if (attendee.isFriend())
                {
                    numberOfFriendsGoing++;
                }
            }

            event.setFriendCount(numberOfFriendsGoing);
            eventCache.save(event);

        }
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

        eventAttendeesAdapter = new EventAttendeesAdapter(new ArrayList<EventDetails.Attendee>(), getActivity());
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

        attendees = eventDetails.getAttendees();
        Collections.sort(attendees, new EventAttendeeComparator(userService.getActiveUserId()));
        eventAttendeesAdapter = new EventAttendeesAdapter(eventDetails.getAttendees(), getActivity());
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
                icon.setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
                break;
            case EAT_OUT:
                icon.setIcon(MaterialDrawableBuilder.IconValue.FOOD);
                break;
            case DRINKS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.MARTINI);
                break;
            case CAFE:
                icon.setIcon(MaterialDrawableBuilder.IconValue.COFFEE);
                break;
            case MOVIES:
                icon.setIcon(MaterialDrawableBuilder.IconValue.THEATER);
                break;
            case OUTDOORS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.BIKE);
                break;
            case SPORTS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.TENNIS);
                break;
            case INDOORS:
                icon.setIcon(MaterialDrawableBuilder.IconValue.XBOX_CONTROLLER);
                break;
            case SHOPPING:
                icon.setIcon(MaterialDrawableBuilder.IconValue.SHOPPING);
                break;
            default:
                icon.setIcon(MaterialDrawableBuilder.IconValue.BULLETIN_BOARD);
        }

        iconContainer.setBackground(DrawableFactory.randomIconBackground());

        title.setText(event.getTitle());

        if (event.getLocation().getName() == null || event.getLocation().getName().isEmpty())
        {
            location.setText(R.string.event_details_no_location);
            location.setOnClickListener(null);
        } else
        {
            location.setText(event.getLocation().getName());
        }

        DateTime start = event.getStartTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        dateTime.setText(start.toString(timeFormatter) + " (" + start.toString(dateFormatter) + ")");

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
        menu.findItem(R.id.action_home).setVisible(false);
        menu.findItem(R.id.action_finalize_event).setVisible(false);
        menu.findItem(R.id.action_delete_event).setVisible(false);
        menu.findItem(R.id.action_add_phone).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_edit_event).setVisible(true);
        menu.findItem(R.id.action_notifications).setVisible(false);

        menu.findItem(R.id.action_edit_event).setIcon(pencilDrawable);

        menu.findItem(R.id.action_edit_event)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    if (EventUtils.canEdit(event, userService
                            .getActiveUserId()) == EventUtilsConstants.CAN_EDIT)
                    {
                        if (areEventDetailsFetched)
                        {
//                        EditEventFragment editEventFragment = new EditEventFragment();
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT, event);
//                        bundle.putSerializable(BundleKeys.EDIT_EVENT_FRAGMENT_EVENT_DETAILS, eventDetails);
//                        editEventFragment.setArguments(bundle);
//                        FragmentUtils.changeFragment(fragmentManager, editEventFragment);
                            FragmentUtils
                                    .changeFragment(fragmentManager, reaper.android.app.ui.screens.edit.EditEventFragment
                                            .newInstance(event, eventDetails));
                        }
                    }
                    else if (EventUtils.canEdit(event, userService
                            .getActiveUserId()) == EventUtilsConstants.CANNOT_EDIT_LOCKED)
                    {
                        Snackbar.make(getView(), R.string.cannot_edit_event_locked, Snackbar.LENGTH_LONG)
                                .show();

                    }
                    else if (EventUtils.canEdit(event, userService
                            .getActiveUserId()) == EventUtilsConstants.CANNOT_EDIT_NOT_GOING)
                    {
                        Snackbar.make(getView(), R.string.cannot_edit_event_not_going, Snackbar.LENGTH_LONG)
                                .show();
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
            if (event.getLocation().getLatitude() != null && event.getLocation().getLongitude() != null)
            {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + event.getLocation().getLatitude() + "," + event.getLocation().getLongitude()));
                startActivity(intent);
            } else
            {
                Snackbar.make(getView(), R.string.location_not_available_on_map, Snackbar.LENGTH_LONG).show();
            }
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
        Toast.makeText(getActivity(), name + " invited you", Toast.LENGTH_LONG).show();
    }
}
