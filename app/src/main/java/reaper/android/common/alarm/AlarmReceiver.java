package reaper.android.common.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.config.MemoryCacheKeys;
import reaper.android.app.model.Event;
import reaper.android.app.ui.screens.FlowEntry;
import reaper.android.app.ui.screens.launch.LauncherActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by harsh on 31/10/15.
 */
public class AlarmReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (!ifAppRunningInForeground())
        {
            fetchEvents(context);
        }
    }

    private void fetchEvents(final Context context)
    {
        EventCache eventCache = CacheManager.getEventCache();

        eventCache.getEvents().subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Event>>()
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
            public void onNext(List<Event> events)
            {
                List<Event> eventsToStartShortly = new ArrayList<Event>();

                for (Event event : events)
                {

                    if (event.getStartTime().isBefore(DateTime.now().plusHours(1)))
                    {
//                        buildNotification(event, context);

                        eventsToStartShortly.add(event);
                    }
                }

                buildNotification(eventsToStartShortly, context);
            }
        });
    }

    private void buildNotification(List<Event> events, Context context)
    {
//        Intent intent = new Intent(context, LauncherActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
        int requestCode = ("someString" + System.currentTimeMillis()).hashCode();

        if (events.size() == 0)
        {
        }
        else if (events.size() == 1)
        {

            if (events.get(0) != null)
            {
//                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "yes");
//                intent.putExtra("event_id", events.get(0).getId());
//                intent.putExtra(BundleKeys.POPUP_STATUS_DIALOG, true);
//
//                intent.putExtra("randomRequestCode", requestCode);

//                PendingIntent pendingIntent = PendingIntent
//                        .getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

                String eventId = events.get(0).getId();
                Intent launcherIntent = LauncherActivity
                        .callingIntent(context, FlowEntry.DETAILS_WITH_STATUS_DIALOG, eventId);

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, requestCode, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.app_icon)
                        .setContentTitle(events.get(0).getTitle())
                        .setContentText(context.getResources()
                                               .getString(R.string.reminder_notification_message))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(2, notificationBuilder.build());
            }
        }
        else if (events.size() > 1)
        {
//            intent.putExtra("randomRequestCode", requestCode);

//            PendingIntent pendingIntent = PendingIntent
//                    .getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);


            Intent launcherIntent = LauncherActivity
                    .callingIntent(context, FlowEntry.HOME, null);

            PendingIntent pendingIntent = PendingIntent
                    .getActivity(context, requestCode, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle("Clanout")
                    .setContentText(events.size() + " clans starting in an hour. Giddy up!")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(2, notificationBuilder.build());
        }
    }

    private boolean ifAppRunningInForeground()
    {
        try
        {
            Boolean isAppInForeground = CacheManager.getMemoryCache()
                                                    .get(MemoryCacheKeys.IS_APP_IN_FOREGROUND, Boolean.class);
            if (isAppInForeground != null)
            {
                return isAppInForeground;
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
