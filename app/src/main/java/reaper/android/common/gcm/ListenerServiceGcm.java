package reaper.android.common.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.service.NotificationService;
import reaper.android.app.communication.Communicator;
import reaper.android.app.model.Notification;
import reaper.android.app.model.util.NotificationFactory;
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
        notificationService = NotificationService.getInstance();
    }

    @Override
    public void onMessageReceived(String from, final Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        final Notification notification = NotificationFactory.create(data);

        NotificationCache notificationCache = CacheManager.getNotificationCache();

        notificationCache.getAllForEvent(notification.getEventId()).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<List<Notification>>()

        {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {
                Log.d("NOTIFICATION", "OnError getAllForEvent --- " + e.getMessage());
            }

            @Override
            public void onNext(List<Notification> notifications)
            {
                Log.d("NOTIFICATION", "onNext getAllForEvent --- " + notifications.size());
                for(Notification notification1 : notifications)
                {
                    Log.d("NOTIFICATION", "onNext getAllForEvent --- " + notification1.getMessage() + "  " + notification1.getEventId());
                }
            }
        });

        notificationCache.getAllForType(notification.getType()).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<List<Notification>>()

        {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {
                Log.d("NOTIFICATION", "OnError getAllForType --- " + e.getMessage());
            }

            @Override
            public void onNext(List<Notification> notifications)
            {
                Log.d("NOTIFICATION", "onNext getAllForType --- " + notifications.size());
                for(Notification notification1 : notifications)
                {
                    Log.d("NOTIFICATION", "onNext getAllForType --- " + notification1.getMessage() + "  " + notification1.getType());
                }
            }
        });

        notificationService.handleNotification(notification);
    }
}
