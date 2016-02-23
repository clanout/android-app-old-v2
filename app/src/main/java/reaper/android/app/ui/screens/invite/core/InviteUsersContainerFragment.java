package reaper.android.app.ui.screens.invite.core;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.event.EventDetailsFetchTrigger;
import reaper.android.app.trigger.event.EventsFetchTrigger;
import reaper.android.app.trigger.user.ManageAppFriendsTrigger;
import reaper.android.app.trigger.user.ManageSMSInviteeTrigger;
import reaper.android.app.ui.screens.MainActivity;
import reaper.android.app.ui.screens.core.BaseFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.app.ui.util.SoftKeyboardHandler;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.communicator.Communicator;

public class InviteUsersContainerFragment extends BaseFragment implements View.OnClickListener {
    private ViewPager viewPager;
    private Button done;
    private TabLayout tabLayout;
    private Drawable checkDrawable;
    private Toolbar toolbar;
    private TextView friendsTabTitle, friendsTabFriendsCount, smsTabTitle, smsTabFriendsCount;

    //    private InviteUsersPagerAdapter inviteUsersPagerAdapter;
    private FragmentManager fragmentManager;
    private EventService eventService;
    private UserService userService;
    private Bus bus;
    private GenericCache genericCache;

    private ArrayList<EventDetails.Invitee> inviteeList;
    private ArrayList<EventDetails.Attendee> attendeeList;
    private Event event;
    private Boolean fromCreateFragment;

    private List<String> invitedAppFriends;
    private List<String> smsInviteePhoneList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.INVITE_USERS_CONTAINER_FRAGMENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_friends_container, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vp_invite_friends_container);
        done = (Button) view.findViewById(R.id.ib_invite_friends_container_done);
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

        invitedAppFriends = new ArrayList<>();
        smsInviteePhoneList = new ArrayList<>();
        inviteeList = new ArrayList<>();
        attendeeList = new ArrayList<>();

        generateDrawables();

        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        userService = UserService.getInstance();
        fragmentManager = getActivity().getFragmentManager();

        genericCache = CacheManager.getGenericCache();

        InviteUsersPagerAdapter inviteUsersPagerAdapter = new InviteUsersPagerAdapter(getChildFragmentManager(), new ArrayList<EventDetails.Invitee>(), new ArrayList<EventDetails.Attendee>(), event);
        viewPager.setAdapter(inviteUsersPagerAdapter);
        viewPager.setOffscreenPageLimit(1);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);

                tabLayout.getTabAt(0).setCustomView(R.layout.tab_invite_friends);
                tabLayout.getTabAt(1).setCustomView(R.layout.tab_invite_friends);

                friendsTabTitle = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.tabTitle);
                friendsTabFriendsCount = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.tabFriendsCount);

                smsTabTitle = (TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.tabTitle);
                smsTabFriendsCount = (TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.tabFriendsCount);

                friendsTabTitle.setText("FRIENDS");
                smsTabTitle.setText("PHONEBOOK");

                friendsTabFriendsCount.setVisibility(View.GONE);
                smsTabFriendsCount.setVisibility(View.GONE);

                tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

                        viewPager.setCurrentItem(tab.getPosition());

                        switch (tab.getPosition()) {
                            case 0:
                                friendsTabTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                                smsTabTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_title));

                                break;
                            case 1:
                                smsTabTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                                friendsTabTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_title));
                                break;
                        }
                    }
                });
            }
        });

        done.setOnClickListener(this);
    }

    private void generateDrawables() {
        checkDrawable = MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(ContextCompat.getColor(getActivity(), R.color.primary))
                .setSizeDp(24)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Invite Friends");

        genericCache.put(GenericCacheKeys.ACTIVE_FRAGMENT, BackstackTags.INVITE_USERS_CONTAINER);
        bus.register(this);
        if (!fromCreateFragment) {
            eventService.fetchEventDetails(event.getId());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }


    @Subscribe
    public void onManageFacebookFriendsTriggerReceived(ManageAppFriendsTrigger trigger) {

        if (trigger.isSelected()) {

            EventDetails.Invitee invitee = new EventDetails.Invitee();
            invitee.setId(trigger.getId());

            if (!inviteeList.contains(invitee)) {

                if (!(invitedAppFriends.contains(trigger.getId()))) {

                    invitedAppFriends.add(trigger.getId());
                }
            }
        } else {

            if (invitedAppFriends.contains(trigger.getId())) {

                invitedAppFriends.remove(trigger.getId());
            }
        }

        if (invitedAppFriends.size() == 0) {
            friendsTabTitle.setText("FRIENDS");
            friendsTabFriendsCount.setVisibility(View.GONE);
        } else {
            friendsTabTitle.setText("FRIENDS");
            friendsTabFriendsCount.setVisibility(View.VISIBLE);
            friendsTabFriendsCount.setText(" " + invitedAppFriends.size());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_invite_friends_container_done) {

            SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

            if (invitedAppFriends.size() != 0) {
                eventService.inviteUsers(event.getId(), invitedAppFriends);

                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.FACEBOOK_FRIENDS_INVITED, "user - " + userService.getSessionUserId() + " event - " + event.getId() + " invitee count - " + invitedAppFriends.size());
            }

            if (smsInviteePhoneList.size() != 0) {
                eventService.inviteThroughSMS(event.getId(), smsInviteePhoneList);

                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.BUTTON_CLICK, GoogleAnalyticsConstants.PHONE_CONTACTS_INVITED_THROUGH_SMS, "user - " + userService.getSessionUserId() + " event - " + event.getId() + " invitee count - " + smsInviteePhoneList.size());
            }


            if (genericCache.get(GenericCacheKeys.HAS_SEEN_INVITE_POPUP) == null) {
                if (smsInviteePhoneList.size() == 0) {
                    eventService.fetchEvents(LocationService_.getInstance().getCurrentLocation().getZone());

                } else {

                    displayInvitePopUp(smsInviteePhoneList.size());
                }
            } else {

                eventService.fetchEvents(LocationService_.getInstance().getCurrentLocation().getZone());
            }

        }
    }

    private void displayInvitePopUp(int smsInviteesSize) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

//        if(smsInviteesSize == 1) {
//            builder.setMessage("You have invited " + smsInviteesSize + " friend who is not using clanOut currently. We have sent them a free SMS.");
//        }else{
//            builder.setMessage("You have invited " + smsInviteesSize + " friends who are not using clanOut currently. We have sent them a free SMS.");
//        }

        int invitedFriendCount = smsInviteePhoneList.size() + invitedAppFriends.size();

        if (invitedFriendCount != 0) {
            builder.setMessage(R.string.friends_invited_message);
        }

        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                genericCache.put(GenericCacheKeys.HAS_SEEN_INVITE_POPUP, true);

                eventService.fetchEvents(LocationService_.getInstance().getCurrentLocation().getZone());
            }
        });

        builder.create().show();
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
            eventService.fetchEvents(LocationService_.getInstance().getCurrentLocation().getZone());
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

        if (smsInviteePhoneList.size() == 0) {
            smsTabTitle.setText("PHONEBOOK");
            smsTabFriendsCount.setVisibility(View.GONE);
        } else {
            smsTabTitle.setText("PHONEBOOK");
            smsTabFriendsCount.setVisibility(View.VISIBLE);
            smsTabFriendsCount.setText(" " + smsInviteePhoneList.size());
        }
    }
}
