package reaper.android.common.gcm;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.service.EventService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.communication.Communicator;
import reaper.android.app.model.Notification;
import reaper.android.app.model.util.NotificationFactory;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.ChatService_;
import reaper.android.app.service._new.GcmService_;
import reaper.android.app.service._new.GoogleService_;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.service._new.PhonebookService_;
import reaper.android.app.service._new.WhatsappService_;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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

        NotificationFactory.create(data)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Notification>()
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
                    public void onNext(Notification notification)
                    {
                        notificationService.handleNotification(notification);
                    }
                });
    }
}
