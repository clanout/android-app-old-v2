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
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.communication.Communicator;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui._core.BaseActivity;
import reaper.android.app.ui.screens.chat.ChatActivity;
import reaper.android.app.ui.screens.details.EventDetailsActivity;
import reaper.android.app.ui.screens.home.HomeFragment;
import reaper.android.app.ui.screens.notifications.NotificationActivity;
import reaper.android.app.ui.util.FragmentUtils;
import reaper.android.common.analytics.AnalyticsHelper;

public class MainActivity extends BaseActivity
{
    private static final String ARG_FLOW_ENTRY = "arg_flow_entry";
    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_EVENT_LIST = "arg_event_list";

    public static Intent callingIntent(Context context, @FlowEntry int flowEntry, String eventId, ArrayList<Event> events)
    {
        Intent intent = new Intent(context, MainActivity.class);

        if (flowEntry == FlowEntry.DETAILS || flowEntry == FlowEntry.DETAILS_WITH_STATUS_DIALOG)
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
        else if (flowEntry == FlowEntry.CHAT)
        {
            if (eventId == null || eventId.isEmpty())
            {
                flowEntry = FlowEntry.HOME;
            }
            else
            {
                intent.putExtra(ARG_EVENT_ID, eventId);
            }
        }

        intent.putExtra(ARG_FLOW_ENTRY, flowEntry);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return intent;
    }

    private android.app.FragmentManager fragmentManager;
    private Bus bus;
    private EventService eventService;
    private UserService userService;

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

        eventService = EventService.getInstance();
        userService = UserService.getInstance();
        fragmentManager = getFragmentManager();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        genericCache = CacheManager.getGenericCache();

        if (genericCache.get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS) == null)
        {
            eventService.getEventSuggestions();
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
                        .getSessionUserId() + " time - " + DateTime.now(DateTimeZone.UTC)
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
    }

    @Override
    public void onBackPressed()
    {
        finish();
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
                navigateToChatFragment(eventId);
                break;
        }
    }

    private void navigateToHomeFragment()
    {
        FragmentUtils.changeFragment(fragmentManager, new HomeFragment());
    }

    private void navigateToNotificationsFragment()
    {
        startActivity(NotificationActivity.callingIntent(this));
        finish();
    }

    private void navigateToChatFragment(String eventId)
    {
        startActivity(ChatActivity.callingIntent(this, eventId));
        finish();
    }

    private void navigateToDetailsFragment(ArrayList<Event> eventList, String eventId, boolean isDialogShown)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId));
    }
}
