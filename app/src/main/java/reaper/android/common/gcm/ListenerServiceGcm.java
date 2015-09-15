package reaper.android.common.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.service.NotificationService;
import reaper.android.common.notification.Notification;
import reaper.android.common.notification.NotificationFactory;
import rx.Subscriber;
import timber.log.Timber;

public class ListenerServiceGcm extends GcmListenerService
{
    private NotificationService notificationService;
    private Bus bus;

    public ListenerServiceGcm()
    {
//        bus = Communicator.getInstance().getBus();
//        notificationService = new NotificationService(bus);
    }

    @Override
    public void onMessageReceived(String from, final Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        final Notification notification = NotificationFactory.create(data);
        final NotificationCache notificationCache = CacheManager.getNotificationCache();

        notificationCache.put(notification)
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.v("[NOTIFICATION RECEIVED]" + data.toString());
                        notificationCache.getAll()
                                .subscribe(new Subscriber<List<Notification>>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {

                                    }

                                    @Override
                                    public void onError(Throwable e)
                                    {

                                    }

                                    @Override
                                    public void onNext(List<Notification> notifications)
                                    {
                                        for(Notification n:notifications)
                                        {
                                            Timber.v("[READ] " + n.toString());
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });

//        notificationService.showNotification(notification);

    }


}
