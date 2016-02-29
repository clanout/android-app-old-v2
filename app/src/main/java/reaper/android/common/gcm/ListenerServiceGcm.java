package reaper.android.common.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import reaper.android.app.service.NotificationService;
import reaper.android.app.communication.Communicator;
import reaper.android.app.model.Notification;
import reaper.android.app.model.util.NotificationFactory;
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
        notificationService.handleNotification(notification);
    }
}
