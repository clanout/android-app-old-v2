package reaper.android.app.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.root.Reaper;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.notifications.NotificationsFetchedTrigger;
import reaper.android.common.notification.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationService
{
    private EventApi eventApi;
    private EventCache eventCache;
    private NotificationCache notificationCache;
    private Bus bus;

    public NotificationService(Bus bus)
    {
        this.eventApi = ApiManager.getInstance().getApi(EventApi.class);
        eventCache = CacheManager.getEventCache();
        notificationCache = CacheManager.getNotificationCache();
        this.bus = bus;
    }

    public void handleNotification(Notification notification)
    {
        int notificationType = notification.getType();

        switch (notificationType)
        {
            case Notification.EVENT_CREATED:
                showCreateEventNotification(notification);
                break;
        }
    }

    private void showCreateEventNotification(final Notification notification)
    {

        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        notificationCache.put(notification)
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {
                                        if (ifAppRunningInForeground())
                                        {
                                            // pop notification inside app
                                        } else
                                        {
                                            // build notification
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e)
                                    {

                                    }

                                    @Override
                                    public void onNext(Object o)
                                    {

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                    }
                });
    }

    private boolean ifAppRunningInForeground()
    {
        try
        {
            ActivityManager activityManager = (ActivityManager) Reaper.getReaperContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager
                    .getRunningTasks(1);

            ComponentName componentName = runningTaskInfo.get(0).topActivity;
            if (componentName.getPackageName().equalsIgnoreCase("reaper.android"))
            {
                return true;
            } else
            {
                return false;
            }
        } catch (Exception e)
        {
            return false;
        }
    }

    private Observable<Event> fetchEvent(String eventId)
    {
        return eventApi.fetchEvent(new FetchEventApiRequest(eventId))
                .map(new Func1<FetchEventApiResponse, Event>()
                {
                    @Override
                    public Event call(FetchEventApiResponse fetchEventApiResponse)
                    {
                        return fetchEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void fetchAllNotifications()
    {
        Log.d("APP", "fetching all notifications");
        notificationCache.getAll().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Notification>>()
        {
            @Override
            public void onCompleted()
            {
            }

            @Override
            public void onError(Throwable e)
            {
                Log.d("APP", "onError -------- " + e.getMessage());
                bus.post(new GenericErrorTrigger(ErrorCode.NOTIFICATIONS_FETCH_FAILURE, (Exception) e));
            }

            @Override
            public void onNext(List<Notification> notifications)
            {
                Log.d("APP", "on Next ------- " + notifications.size());
                bus.post(new NotificationsFetchedTrigger(notifications));
            }
        });
    }

    public void deleteAllNotificationsFromCache()
    {
        notificationCache.clear().observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
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
            public void onNext(Object o)
            {

            }
        });
    }

    public void markAllNotificationsAsRead()
    {
        notificationCache.markRead().observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
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
            public void onNext(Object o)
            {

            }
        });
    }
}
