package reaper.android.app.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.BackstackTags;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.config.NotificationConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.FacebookService;
import reaper.android.app.service.GCMService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.trigger.common.BackPressedTrigger;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.event.EventsFetchForActivityTrigger;
import reaper.android.app.trigger.gcm.GcmRegistrationCompleteTrigger;
import reaper.android.app.ui.screens.accounts.AccountsFragment;
import reaper.android.app.ui.screens.details.EventDetailsContainerFragment;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.chat.ChatHelper;
import reaper.android.common.communicator.Communicator;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;
    private Bus bus;
    private UserService userService;
    private GCMService gcmService;
    private EventService eventService;
    private LocationService locationService;
    private FacebookService facebookService;

    private GenericCache genericCache;
    private EventCache eventCache;
    private UserCache userCache;

    private String eventId;
    private boolean isBusRegistered;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bus = Communicator.getInstance().getBus();
        bus.register(this);
        isBusRegistered = true;

        userService = new UserService(bus);
        gcmService = new GCMService(bus);
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        facebookService = new FacebookService(bus);
        fragmentManager = getSupportFragmentManager();

        genericCache = CacheManager.getGenericCache();
        eventCache = CacheManager.getEventCache();
        userCache = CacheManager.getUserCache();

        String shouldGoToDetailsFragment = getIntent().getStringExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT);
        eventId = getIntent().getStringExtra("event_id");

        if (shouldGoToDetailsFragment.equals("yes") && eventId != null)
        {
            eventService.fetchEventsForActivity(locationService.getUserLocation().getZone());
        } else
        {
            String lastNotificationReceived = genericCache.get(Timestamps.NOTIFICATION_RECEIVED_TIMESTAMP);
            String lastFriendRelocatedNotificationReceived = genericCache.get(Timestamps.FRIEND_RELOCATED_NOTIFICATION_TIMESTAMP);

            if (lastNotificationReceived != null && lastFriendRelocatedNotificationReceived != null)
            {
                DateTime lastNotificationTimestamp = DateTime.parse(lastNotificationReceived);
                DateTime lastFriendRelocatedNotificationTimestamp = DateTime.parse(lastFriendRelocatedNotificationReceived);

                if (DateTime.now().getMillis() - lastNotificationTimestamp.getMillis() > Timestamps.NOTIFICATION_NOT_RECEIVED_LIMIT)
                {
                    eventCache.deleteAll();
                }

                if (DateTime.now().getMillis() - lastFriendRelocatedNotificationTimestamp.getMillis() > Timestamps.FRIEND_RELOCATED_NOTIFICATION_NOT_RECEIVED_LIMIT)
                {
                    userCache.deleteFriends();
                }
            }

            DateTime lastFacebookFriendsRefreshTimestamp = genericCache.get(Timestamps.LAST_FACEBOOK_FRIENDS_REFRESHED_TIMESTAMP, DateTime.class);

            if (lastFacebookFriendsRefreshTimestamp != null)
            {

                if (DateTime.now().minusDays(2).isAfter(lastFacebookFriendsRefreshTimestamp))
                {
                    facebookService.getFacebookFriends(true);
                }
            } else
            {
                facebookService.getFacebookFriends(true);
            }

            if (genericCache.get(CacheKeys.GCM_TOKEN) == null)
            {
                if (checkPlayServices())
                {
                    gcmService.register();
                }
            } else
            {
                FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!isBusRegistered)
        {
            bus.register(this);
        }
        getSupportActionBar().setTitle("reap3r");
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
        String activeFragment = genericCache.get(CacheKeys.ACTIVE_FRAGMENT);

        if (activeFragment == null)
        {
            super.onBackPressed();
        }

        if (activeFragment.equals(BackstackTags.HOME))
        {
            finish();
        } else if (activeFragment.equals(BackstackTags.ACCOUNTS))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        } else if (activeFragment.equals(BackstackTags.MANAGE_FRIENDS))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        } else if (activeFragment.equals(BackstackTags.FAQ))
        {
            FragmentUtils.changeFragment(fragmentManager, new AccountsFragment());
        } else if (activeFragment.equals(BackstackTags.CREATE))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        } else if (activeFragment.equals(BackstackTags.INVITE_USERS_CONTAINER))
        {
            bus.post(new BackPressedTrigger(BackstackTags.INVITE_USERS_CONTAINER));
        } else if (activeFragment.equals(BackstackTags.EVENT_DETAILS_CONTAINER))
        {
            FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
        } else if (activeFragment.equals(BackstackTags.EDIT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.EDIT));
        } else if (activeFragment.equals(BackstackTags.CHAT))
        {
            bus.post(new BackPressedTrigger(BackstackTags.CHAT));
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else
            {
                Toast.makeText(this, "This device does not support Google Play Services.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Subscribe
    public void onGcmRegistrationComplete(GcmRegistrationCompleteTrigger trigger)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
            }
        });
    }

    @Subscribe
    public void onEventsFetched(EventsFetchForActivityTrigger trigger)
    {
        List<Event> eventList = trigger.getEvents();

        Event activeEvent = new Event();
        activeEvent.setId(eventId);

        int activePosition = eventList.indexOf(activeEvent);

        EventDetailsContainerFragment eventDetailsContainerFragment = new EventDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_EVENTS, (ArrayList<Event>) eventList);
        bundle.putInt(BundleKeys.EVENT_DETAILS_CONTAINER_FRAGMENT_ACTIVE_POSITION, activePosition);
        eventDetailsContainerFragment.setArguments(bundle);
        FragmentUtils.changeFragment(fragmentManager, eventDetailsContainerFragment);
    }

    @Subscribe
    public void onEventsNotFetched(GenericErrorTrigger trigger)
    {
        if (trigger.getErrorCode() == ErrorCode.EVENTS_FETCH_FOR_ACTIVITY_FAILURE)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
                }
            });
        }
    }

    @Subscribe
    public void onEventEditFailedAsFinalised(GenericErrorTrigger trigger)
    {
        if(trigger.getErrorCode() == ErrorCode.EVENT_EDIT_FAILURE_LOCKED)
        {
            Toast.makeText(this, R.string.event_edit_failed_locked, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEventFinalisationFailed(GenericErrorTrigger trigger)
    {
        if(trigger.getErrorCode() == ErrorCode.EVENT_COULD_NOT_BE_FINALISED)
        {
            Toast.makeText(this, R.string.event_finalisation_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEventUnFinalisationFailed(GenericErrorTrigger trigger)
    {
        if(trigger.getErrorCode() == ErrorCode.EVENT_COULD_NOT_BE_UNFINALISED)
        {
            Toast.makeText(this, R.string.event_unfinalisation_failed, Toast.LENGTH_LONG).show();
        }
    }
}
