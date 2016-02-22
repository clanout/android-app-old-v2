package reaper.android.app.ui.screens;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.chat.ChatFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.screens.notifications.NotificationFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;

public class MainActivity extends BaseActivity
{
    private static final String ARG_FLOW_ENTRY = "arg_flow_entry";
    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_EVENT_LIST = "arg_event_list";

    public static Intent callingIntent(Context context, @FlowEntry int flowEntry, String eventId, ArrayList<Event> events)
    {
        Intent intent = new Intent(context, MainActivity.class);

        if (flowEntry == FlowEntry.DETAILS || flowEntry == FlowEntry.DETAILS_WITH_STATUS_DIALOG || flowEntry == FlowEntry.CHAT)
        {
            if (eventId == null || eventId.isEmpty() || events == null || events.isEmpty())
            {
                flowEntry = FlowEntry.HOME;
            }
            else
            {
                intent.putExtra(ARG_EVENT_ID, eventId);
                intent.putExtra(ARG_EVENT_LIST, events);
            }
        }

        intent.putExtra(ARG_FLOW_ENTRY, flowEntry);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    private android.app.FragmentManager fragmentManager;
    private Bus bus;
    private EventService eventService;
    private UserService userService;
    private FacebookService facebookService;

    private GenericCache genericCache;

    private boolean isBusRegistered;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.MAIN_ACTIVITY);

        setContentView(R.layout.activity_main);

        bus = Communicator.getInstance().getBus();
        bus.register(this);
        isBusRegistered = true;

        eventService = new EventService(bus);
        userService = new UserService(bus);
        facebookService = new FacebookService(bus);
        fragmentManager = getFragmentManager();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        genericCache = CacheManager.getGenericCache();

        if (genericCache.get(GenericCacheKeys.EVENT_SUGGESTIONS) == null)
        {
            eventService.getEventSuggestions();
        }

        DateTime lastFacebookFriendsRefreshTimestamp = genericCache
                .get(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.class);

        if (lastFacebookFriendsRefreshTimestamp != null)
        {
            if (DateTime.now().minusDays(2).isAfter(lastFacebookFriendsRefreshTimestamp))
            {
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.FACEBOOK_FRIEND_REFRESHED_LIMIT_CROSSED, null);
                facebookService.getFacebookFriends(true);
            }
        }
        else
        {
            facebookService.getFacebookFriends(true);
        }

        handleIntent();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!isBusRegistered)
        {
            bus.register(this);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.APP_CLOSED, "user - " + userService
                        .getActiveUserId() + " time - " + DateTime.now(DateTimeZone.UTC)
                                                                  .toString());
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        bus.unregister(this);
        isBusRegistered = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        ChatHelper.disconnectConnection();
    }

    @Override
    public void onBackPressed()
    {
        String activeFragment = genericCache.get(GenericCacheKeys.ACTIVE_FRAGMENT);

        if (activeFragment == null)
        {
            super.onBackPressed();
        }
        else if (activeFragment.equals(BackstackTags.HOME))
        {
            finish();
        }
        else if (activeFragment.equals(BackstackTags.ACCOUNTS))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
        else if (activeFragment.equals(BackstackTags.MANAGE_FRIENDS))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        }
        else if (activeFragment.equals(BackstackTags.FAQ))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        }
        else if (activeFragment.equals(BackstackTags.INVITE_USERS_CONTAINER))
        {
            bus.post(new BackPressedTrigger(BackstackTags.INVITE_USERS_CONTAINER));
        }
        else if (activeFragment.equals(BackstackTags.EVENT_DETAILS_CONTAINER))
        {
            bus.post(new BackPressedTrigger(BackstackTags.EVENT_DETAILS_CONTAINER));
        }
        else if (activeFragment.equals(BackstackTags.EDIT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.EDIT));
        }
        else if (activeFragment.equals(BackstackTags.CHAT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.CHAT));
        }
        else if (activeFragment.equals(BackstackTags.NOTIFICATIONS))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
        else if (activeFragment.equals(BackstackTags.CREATE))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        }
    }

    private void handleIntent()
    {
        Intent sourceIntent = getIntent();

        @FlowEntry int flowEntry = sourceIntent.getIntExtra(ARG_FLOW_ENTRY, FlowEntry.HOME);
        String eventId = sourceIntent.getStringExtra(ARG_EVENT_ID);
        ArrayList<Event> eventList = (ArrayList<Event>) sourceIntent
                .getSerializableExtra(ARG_EVENT_LIST);

        switch (flowEntry)
        {
            case FlowEntry.HOME:
                navigateToHomeFragment();
                break;

            case FlowEntry.NOTIFICATIONS:
                navigateToNotificationsFragment();
                break;

            case FlowEntry.DETAILS:
                navigateToDetailsFragment(eventList, eventId, false);
                break;

            case FlowEntry.DETAILS_WITH_STATUS_DIALOG:
                navigateToDetailsFragment(eventList, eventId, true);
                break;

            case FlowEntry.CHAT:
                navigateToChatFragment(eventList, eventId);
                break;
        }
    }

    private void navigateToHomeFragment()
    {
        FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
    }

    private void navigateToNotificationsFragment()
    {
        FragmentUtils.changeFragment(getFragmentManager(), new NotificationFragment());
    }

    private void navigateToChatFragment(ArrayList<Event> eventList, String eventId)
    {
        Event activeEvent = new Event();
        activeEvent.setId(eventId);
        int activePosition = 0;
        if (eventList.contains(activeEvent))
        {
            activePosition = eventList.indexOf(activeEvent);
        }
        String eventName = eventList.get(activePosition).getTitle();

        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_ID, eventId);
        bundle.putString(BundleKeys.CHAT_FRAGMENT_EVENT_NAME, eventName);
        chatFragment.setArguments(bundle);

        FragmentUtils.changeFragment(getFragmentManager(), chatFragment);
    }

    private void navigateToDetailsFragment(ArrayList<Event> eventList, String eventId, boolean isDialogShown)
    {
        Event activeEvent = new Event();
        activeEvent.setId(eventId);
        int activePosition = 0;
        if (eventList.contains(activeEvent))
        {
            activePosition = eventList.indexOf(activeEvent);
        }

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, eventList);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        bundle.putBoolean(BundleKeys.POPUP_STATUS_DIALOG, isDialogShown);
        eventDetailsContainerFragment.setArguments(bundle);

        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }
}
