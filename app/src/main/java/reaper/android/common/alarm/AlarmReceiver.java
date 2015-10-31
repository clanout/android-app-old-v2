package reaper.android.common.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.List;

import reaper.android.R;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.root.Reaper;
import reaper.android.app.service.AccountsService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.ui.activity.LauncherActivity;
import reaper.android.common.communicator.Communicator;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by harsh on 31/10/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!ifAppRunningInForeground()) {
            fetchEvents(context);
        }
    }

    private void fetchEvents(final Context context) {
        EventCache eventCache = CacheManager.getEventCache();

        eventCache.getEvents().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Event>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Event> events) {

                for (Event event : events) {

                    // TODO -- change condition

                    if ((event.getStartTime().getMillis() - DateTime.now().getMillis() > 1)) {

                        buildNotification(event, context);
                    }
                }
            }
        });
    }

    private void buildNotification(Event event, Context context) {

        Intent intent = new Intent(context, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = ("someString" + System.currentTimeMillis()).hashCode();

        intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "yes");
        intent.putExtra("event_id", event.getId());
        intent.putExtra(BundleKeys.POPUP_STATUS_DIALOG, true);

        intent.putExtra("randomRequestCode", requestCode);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.logo1)
                .setContentTitle(event.getTitle())
                .setContentText(context.getResources().getString(R.string.reminder_notification_message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        ;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) (Math.random() * 1000), notificationBuilder.build());
    }

    private boolean ifAppRunningInForeground() {

        GenericCache genericCache = CacheManager.getGenericCache();

        if (genericCache.get(CacheKeys.IS_APP_IN_FOREGROUND).equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }
}
