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
import reaper.android.app.service.NotificationService;
import reaper.android.app.trigger.event.NewEventAddedTrigger;
import reaper.android.app.ui.activity.LauncherActivity;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.notification.Notification;
import reaper.android.common.notification.NotificationFactory;
import reaper.android.common.notification.NotificationHelper;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ListenerServiceGcm extends GcmListenerService
{
    private NotificationService notificationService;
    private Bus bus;

    public ListenerServiceGcm()
    {
        bus = Communicator.getInstance().getBus();
        notificationService = new NotificationService(bus);
    }

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        final Notification notification = NotificationFactory.create(data);
        notificationService.showNotification(notification);

    }


}
