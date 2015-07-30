package reaper.android.app.ui.screens.invite.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.communicator.Communicator;

public class InviteUsersContainerFragment extends Fragment implements TabLayout.OnTabSelectedListener, InviteeListCommunicator, View.OnClickListener
{
    private ViewPager viewPager;
    private ImageButton done;
    private TabLayout tabLayout;

    private InviteUsersPagerAdapter inviteUsersPagerAdapter;
    private FragmentManager fragmentManager;
    private EventService eventService;
    private LocationService locationService;
    private Bus bus;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private String eventId;
    private Boolean fromCreateFragment;

    private List<String> invitedFacebookFriends;
    private List<String> invitedPhoneContacts;
    private List<String> invitedUsers;

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
        View view = inflater.inflate(R.layout.fragment_invite_friends_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_invite_friends_container);
        done = (ImageButton) view.findViewById(R.id.ib_invite_friends_container_done);
        tabLayout = (TabLayout) view.findViewById(R.id.tl_invite_friends_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            throw new IllegalStateException("Bundle is null");
        }

        eventId = (String) bundle.get("event_id");
        if (eventId == null)
        {
            throw new IllegalStateException("Event Id is null");
        }

        fromCreateFragment = (Boolean) bundle.get("from_create_fragment");
        if (fromCreateFragment == null)
        {
            throw new IllegalStateException("fromCreateFragment can't be null");
        }

        invitedFacebookFriends = new ArrayList<>();
        invitedPhoneContacts = new ArrayList<>();
        invitedUsers = new ArrayList<>();

        bus = Communicator.getInstance().getBus();
        bus.register(this);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getActivity().getSupportFragmentManager();

        inviteUsersPagerAdapter = new InviteUsersPagerAdapter(getChildFragmentManager(), new ArrayList<EventDetails.Invitee>(), this);
        viewPager.setAdapter(inviteUsersPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);

        done.setOnClickListener(this);

        if (!fromCreateFragment)
        {
            eventService.fetchEventDetails(eventId);
        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onPause()
    {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void manageFacebookFriends(String id)
    {
        if (invitedFacebookFriends.contains(id))
        {
            invitedFacebookFriends.remove(id);
        }
        else
        {
            invitedFacebookFriends.add(id);
        }
    }

    @Override
    public void managePhoneContacts(String id)
    {
        if (invitedPhoneContacts.contains(id))
        {
            invitedPhoneContacts.remove(id);
        }
        else
        {
            invitedPhoneContacts.add(id);
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.ib_invite_friends_container_done)
        {
            invitedUsers = new ArrayList<>();
            invitedUsers.addAll(invitedFacebookFriends);
            invitedUsers.addAll(invitedPhoneContacts);

            eventService.inviteUsers(eventId, invitedUsers);
            eventService.deleteEventDetailsCacheFor(eventId);

            eventService.fetchEvents(locationService.getUserLocation().getZone());

        }
    }

    @Subscribe
    public void onEventDetailsFetched(EventDetailsFetchTrigger trigger)
    {
        if (!fromCreateFragment)
        {
            inviteeList = (ArrayList<EventDetails.Invitee>) trigger.getEventDetails().getInvitee();

            inviteUsersPagerAdapter = new InviteUsersPagerAdapter(getChildFragmentManager(), inviteeList, this);
            viewPager.setAdapter(inviteUsersPagerAdapter);
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger)
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

}
