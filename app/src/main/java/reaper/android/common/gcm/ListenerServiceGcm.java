package reaper.android.common.gcm;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;

import java.lang.reflect.Type;
import java.util.Map;

import reaper.android.R;
import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.SQLiteCacheHelper;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.NotificationTypes;
import reaper.android.app.service.EventService;
import reaper.android.common.communicator.Communicator;

public class ListenerServiceGcm extends GcmListenerService
{
    private Bus bus;
    private EventService eventService;
    private UserCache userCache;
    private EventCache eventCache;

    private static Type type;

    public ListenerServiceGcm()
    {
        bus = Communicator.getInstance().getBus();
        eventService = new EventService(bus);
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

        if (notificationType.equals(NotificationTypes.EVENT_ADDED))
        {
            eventService.fetchEvent(notificationAttributes.get("event_id"));
            buildNotification(message);
        }
        else if (notificationType.equals(NotificationTypes.EVENT_REMOVED))
        {
            eventService.deleteEvent(notificationAttributes.get("event_id"));
        }
        else if (notificationType.equals(NotificationTypes.EVENT_UPDATED))
        {
            eventService.fetchEvent(notificationAttributes.get("event_id"));
            buildNotification(message);
        }
        else if (notificationType.equals(NotificationTypes.FRIEND_ADDED))
        {
            userCache.evictFriendsCache();
            buildNotification(message);
        }
        else if (notificationType.equals(NotificationTypes.FRIEND_REMOVED))
        {
            userCache.evictFriendsCache();
        }
        else if (notificationType.equals(NotificationTypes.INVITED_TO_EVENT))
        {
            eventCache.invalidateCompletely(notificationAttributes.get("event_id"));
            eventService.fetchEvent(notificationAttributes.get("event_id"));
        }
    }

    private void buildNotification(String message)
    {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_important)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
