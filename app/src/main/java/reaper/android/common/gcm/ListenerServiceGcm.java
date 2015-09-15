package reaper.android.common.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import reaper.android.app.service.NotificationService;
import reaper.android.common.communicator.Communicator;
import reaper.android.common.notification.Notification;
import reaper.android.common.notification.NotificationFactory;
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
    public void onMessageReceived(String from, final Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        final Notification notification = NotificationFactory.create(data);
        notificationService.handleNotification(notification);
    }
}
