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

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.NotificationConstants;
import reaper.android.app.config.Timestamps;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.trigger.event.NewEventAddedTrigger;
import reaper.android.app.ui.activity.LauncherActivity;
import reaper.android.common.notification.Notification;
import reaper.android.common.notification.NotificationFactory;
import reaper.android.common.notification.NotificationHelper;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ListenerServiceGcm extends GcmListenerService
{
    private Bus bus;
    private EventService eventService;
    private LocationService locationService;
    private UserCache userCache;
    private EventCache eventCache;
    private GenericCache genericCache;

    private static Type type = new TypeToken<Map<String, String>>()
    {
    }.getType();

    public ListenerServiceGcm()
    {
//        bus = Communicator.getInstance().getBus();
//        eventService = new EventService(bus);
//        locationService = new LocationService(bus);
//        userCache = CacheManager.getUserCache();
//        eventCache = CacheManager.getEventCache();
//        genericCache = CacheManager.getGenericCache();
//        type = new TypeToken<Map<String, String>>()
//        {
//        }.getType();
//
        Timber.d("GCM Listener init");
//        DatabaseManager.init(this);
    }

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        final Notification notification = NotificationFactory.create(data);

        NotificationCache notificationCache = CacheManager.getNotificationCache();

        notificationCache.put(notification)
                         .observeOn(Schedulers.newThread())
                         .subscribe(new Subscriber<Object>()
                         {
                             @Override
                             public void onCompleted()
                             {
                                 NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ListenerServiceGcm.this)
                                         .setSmallIcon(NotificationHelper
                                                 .getIcon(notification.getType()))
                                         .setContentTitle(notification.getTitle())
                                         .setContentText(notification.getMessage())
                                         .setAutoCancel(true);


                                 NotificationManager notificationManager =
                                         (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                 notificationManager
                                         .notify(notification.getId(), notificationBuilder.build());
                             }

                             @Override
                             public void onError(Throwable e)
                             {
                                 Timber.e("Unable to put notification :" + e.getMessage());
                             }

                             @Override
                             public void onNext(Object o)
                             {

                             }
                         });
    }

    private void doProcessing(Bundle data)
    {
        String notificationType = data.getString("type");
        Map<String, String> notificationAttributes = GsonProvider.getGson().fromJson(data
                .getString("parameters"), type);
        String message = data.getString("message");

        Timber.d("[NOTIFICATION] " + message);

        if (notificationType.equals(NotificationConstants.EVENT_ADDED))
        {
            genericCache.put(Timestamps.NOTIFICATION_RECEIVED_TIMESTAMP, DateTime.now());
            eventService.fetchEvent(notificationAttributes.get("event_id"), false);

            if (!checkIfAppRunningInForeground())
            {
                buildNotification(message, NotificationConstants.EVENT_ADDED_TITLE, true, notificationAttributes
                        .get("event_id"));
            }
            else
            {
                bus.post(new NewEventAddedTrigger());
            }
        }
        else if (notificationType.equals(NotificationConstants.EVENT_REMOVED))
        {
            genericCache.put(Timestamps.NOTIFICATION_RECEIVED_TIMESTAMP, DateTime.now());
            eventService.deleteEvent(notificationAttributes.get("event_id"));
        }
        else if (notificationType.equals(NotificationConstants.EVENT_UPDATED))
        {
            Timber.v("Event Updated Notification start");
            genericCache.put(Timestamps.NOTIFICATION_RECEIVED_TIMESTAMP, DateTime.now());
            eventCache.deleteCompletely(notificationAttributes.get("event_id"));
            eventService.fetchEvent(notificationAttributes.get("event_id"), true);

            if (!checkIfAppRunningInForeground())
            {
                buildNotification(message, NotificationConstants.EVENT_UPDATED_TITLE, true, notificationAttributes
                        .get("event_id"));
            }

            Timber.v("Event Updated Notification end");
        }
        else if (notificationType.equals(NotificationConstants.FRIEND_RELOCATED))
        {
            genericCache.put(Timestamps.FRIEND_RELOCATED_NOTIFICATION_TIMESTAMP, DateTime.now());
            userCache.deleteFriends();

            String zone = notificationAttributes.get("zone");

            if (zone.equals(locationService.getUserLocation().getZone()))
            {
                String friendName = notificationAttributes.get("name");
                buildNotification(friendName + " is in " + zone + ". You can invite " + friendName + " to local events.", NotificationConstants.FRIEND_RELOCATED_TITLE, false, "");
            }
        }
        else if (notificationType.equals(NotificationConstants.EVENT_INVITATION))
        {
            genericCache.put(Timestamps.NOTIFICATION_RECEIVED_TIMESTAMP, DateTime.now());
            eventCache.deleteCompletely(notificationAttributes.get("event_id"));
            eventService.fetchEvent(notificationAttributes.get("event_id"), true);
            buildNotification(message, NotificationConstants.INVITE_RECEIVED_TITLE, true, notificationAttributes
                    .get("event_id"));
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

        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_add_person)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) (Math.random() * 1000), notificationBuilder.build());
    }

    private boolean checkIfAppRunningInForeground()
    {
        try
        {
            ActivityManager activityManager = (ActivityManager) this
                    .getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager
                    .getRunningTasks(1);

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
