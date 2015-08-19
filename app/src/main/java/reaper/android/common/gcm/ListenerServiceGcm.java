package reaper.android.common.gcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.SQLiteCacheHelper;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.NotificationConstants;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.ui.activity.LauncherActivity;
import reaper.android.common.communicator.Communicator;

public class ListenerServiceGcm extends GcmListenerService
{
    private Bus bus;
    private EventService eventService;
    private LocationService locationService;
    private UserCache userCache;
    private EventCache eventCache;

    private static Type type;

    public ListenerServiceGcm()
    {
        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
        locationService = new LocationService(bus);
        userCache = new UserCache();
        eventCache = new EventCache();
        type = new TypeToken<Map<String, String>>()
        {
        }.getType();

        SQLiteCacheHelper.init(this);
    }

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Log.d("APP", "notification received ----- " + data.getString("message"));
        doProcessing(data);
    }

    private void doProcessing(Bundle data)
    {
        String notificationType = data.getString("type");
        Map<String, String> notificationAttributes = GsonProvider.getGson().fromJson(data.getString("parameters"), type);
        String message = data.getString("message");

        if (notificationType.equals(NotificationConstants.EVENT_ADDED))
        {
            eventService.fetchEvent(notificationAttributes.get("event_id"), false);

            if (!checkIfAppRunningInForeground())
            {
                buildNotification(message, NotificationConstants.EVENT_ADDED_TITLE, true, notificationAttributes.get("event_id"));
            }
        }
        else if (notificationType.equals(NotificationConstants.EVENT_REMOVED))
        {
            eventService.deleteEvent(notificationAttributes.get("event_id"));
        }
        else if (notificationType.equals(NotificationConstants.EVENT_UPDATED))
        {
            eventCache.invalidateCompletely(notificationAttributes.get("event_id"));
            eventService.fetchEvent(notificationAttributes.get("event_id"), true);

            if (!checkIfAppRunningInForeground())
            {
                buildNotification(message, NotificationConstants.EVENT_UPDATED_TITLE, true, notificationAttributes.get("event_id"));
            }
        }
        else if (notificationType.equals(NotificationConstants.FRIEND_RELOCATED))
        {
            userCache.evictFriendsCache();

            String zone = notificationAttributes.get("zone");
            if (zone.equals(locationService.getUserLocation().getZone()))
            {
                String friendName = notificationAttributes.get("name");
                buildNotification(friendName + " is in " + zone + ". You can invite " + friendName + " to local events.", NotificationConstants.FRIEND_RELOCATED_TITLE, false, "");
            }
        }
        else if (notificationType.equals(NotificationConstants.EVENT_INVITATION))
        {
            eventCache.invalidateCompletely(notificationAttributes.get("event_id"));
            eventService.fetchEvent(notificationAttributes.get("event_id"), true);
            buildNotification(message, NotificationConstants.INVITE_RECEIVED_TITLE, true, notificationAttributes.get("event_id"));
        }
    }

    private void buildNotification(String message, String title, boolean shouldGoToDetailsFragment, String eventId)
    {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (shouldGoToDetailsFragment)
        {
            intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "yes");
            intent.putExtra("event_id", eventId);
        }
        else
        {
            intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "no");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_important)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private boolean checkIfAppRunningInForeground()
    {
        try
        {
            ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);

            ComponentName componentName = runningTaskInfo.get(0).topActivity;
            if (componentName.getPackageName().equalsIgnoreCase("reaper.android"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
