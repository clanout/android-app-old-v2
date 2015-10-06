package reaper.android.app.ui.screens.invite.core;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.user.ManageAppFriendsTrigger;
import reaper.android.app.trigger.user.ManagePhoneContactsTrigger;
import reaper.android.app.trigger.user.ManageSMSInviteeTrigger;
import reaper.android.app.ui.activity.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.details.ZoomOutPageTransformer;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class InviteUsersContainerFragment extends BaseFragment implements TabLayout.OnTabSelectedListener, View.OnClickListener {
    private ViewPager viewPager;
    private ImageButton done;
    private TabLayout tabLayout;
    private Drawable checkDrawable;
    private Toolbar toolbar;

    //    private InviteUsersPagerAdapter inviteUsersPagerAdapter;
    private FragmentManager fragmentManager;
    private EventService eventService;
    private LocationService locationService;
    private Bus bus;
    private GenericCache genericCache;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private ArrayList<EventDetails.Attendee> attendeeList;
    private Event event;
    private Boolean fromCreateFragment;

    private List<String> invitedFacebookFriends;
    private List<String> invitedPhoneContacts;
    private List<String> invitedUsers;
    private List<String> smsInviteePhoneList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_friends_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_invite_friends_container);
        done = (ImageButton) view.findViewById(R.id.ib_invite_friends_container_done);
        tabLayout = (TabLayout) view.findViewById(R.id.tl_invite_friends_container);
        toolbar = (Toolbar) view.findViewById(R.id.tb_fragment_invite_container);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle == null) {
            throw new IllegalStateException("Bundle is null");
        }

        event = (Event) bundle.get(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_EVENT);
        if (event == null) {
            throw new IllegalStateException("Event Id is null");
        }

        fromCreateFragment = (Boolean) bundle.get(BundleKeys.INVITE_USERS_CONTAINER_FRAGMENT_FROM_CREATE_FRAGMENT);
        if (fromCreateFragment == null) {
            throw new IllegalStateException("fromCreateFragment can't be null");
        }

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        invitedFacebookFriends = new ArrayList<>();
        invitedPhoneContacts = new ArrayList<>();
        invitedUsers = new ArrayList<>();
        smsInviteePhoneList = new ArrayList<>();

        generateDrawables();

        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        fragmentManager = getActivity().getFragmentManager();

        genericCache = CacheManager.getGenericCache();

        InviteUsersPagerAdapter inviteUsersPagerAdapter = new InviteUsersPagerAdapter(getChildFragmentManager(), new ArrayList<EventDetails.Invitee>(), new ArrayList<EventDetails.Attendee>(), event);
        viewPager.setAdapter(inviteUsersPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
        tabLayout.setOnTabSelectedListener(this);

        done.setOnClickListener(this);
        done.setImageDrawable(checkDrawable);
    }

    private void generateDrawables() {
        checkDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(getResources().getColor(R.color.whity))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_USERS_CONTAINER_FRAGMENT);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Invite Friends");

        genericCache.put(CacheKeys.ACTIVE_FRAGMENT, BackstackTags.INVITE_USERS_CONTAINER);
        bus.register(this);
        if (!fromCreateFragment) {
            eventService.fetchEventDetails(event.getId());
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onManageFacebookFriendsTriggerReceived(ManageAppFriendsTrigger trigger) {

        if (trigger.isSelected()) {
            if (!(invitedFacebookFriends.contains(trigger.getId()))) {
                invitedFacebookFriends.add(trigger.getId());
            }
        } else {

            if (invitedPhoneContacts.contains(trigger.getId())) {
                invitedFacebookFriends.remove(trigger.getId());
            }
        }
    }

    @Subscribe
    public void onManagePhoneContactsTriggerReceived(ManagePhoneContactsTrigger trigger) {
        if (trigger.isSelected()) {
            if (!(invitedPhoneContacts.contains(trigger.getId()))) {
                invitedPhoneContacts.add(trigger.getId());
            }
        } else {

            if (invitedPhoneContacts.contains(trigger.getId())) {
                invitedPhoneContacts.remove(trigger.getId());
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_invite_friends_container_done) {
            invitedUsers = new ArrayList<>();
            invitedUsers.addAll(invitedFacebookFriends);
            invitedUsers.addAll(invitedPhoneContacts);

            if (invitedUsers.size() != 0) {
                eventService.inviteUsers(event.getId(), invitedUsers);
            }

            if(smsInviteePhoneList.size() != 0)
            {
                eventService.inviteThroughSMS(event.getId(), smsInviteePhoneList);
            }

            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }

    @Subscribe
    public void onEventDetailsFetched(EventDetailsFetchTrigger trigger) {
        if (!fromCreateFragment) {
            inviteeList = (ArrayList<EventDetails.Invitee>) trigger.getEventDetails().getInvitee();
            attendeeList = (ArrayList<EventDetails.Attendee>) trigger.getEventDetails().getAttendees();

            InviteUsersPagerAdapter inviteUsersPagerAdapterA = new InviteUsersPagerAdapter(getChildFragmentManager(), inviteeList, attendeeList, event);
            viewPager.setAdapter(inviteUsersPagerAdapterA);
        }
    }

    @Subscribe
    public void onEventsFetched(EventsFetchTrigger trigger) {
        List<Event> events = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(event.getId());

        int activePosition = events.indexOf(activeEvent);

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) events);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }

    @Subscribe
    public void backPressed(BackPressedTrigger trigger) {
        if (trigger.getActiveFragment().equals(BackstackTags.INVITE_USERS_CONTAINER)) {
            eventService.fetchEvents(locationService.getUserLocation().getZone());
        }
    }

    @Subscribe
    public void onSMSInviteTriggerReceived(ManageSMSInviteeTrigger trigger) {

        if (trigger.isSelected()) {
            if (!(smsInviteePhoneList.contains(trigger.getPhone()))) {
                smsInviteePhoneList.add(trigger.getPhone());
            }
        } else {

            if (smsInviteePhoneList.contains(trigger.getPhone())) {
                smsInviteePhoneList.remove(trigger.getPhone());
            }
        }
    }
}
