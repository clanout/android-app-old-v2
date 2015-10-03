package reaper.android.app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.List;

import reaper.android.R;
import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Event;
import reaper.android.app.root.Reaper;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.notifications.NewNotificationReceivedTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsAvailableTrigger;
import reaper.android.app.trigger.notifications.NewNotificationsNotAvailableTrigger;
import reaper.android.app.trigger.notifications.NotificationsFetchedTrigger;
import reaper.android.app.ui.activity.LauncherActivity;
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
    private GenericCache genericCache;
    private UserCache userCache;
    private UserService userService;
    private Bus bus;

    public NotificationService(Bus bus)
    {
        this.eventApi = ApiManager.getInstance().getApi(EventApi.class);
        eventCache = CacheManager.getEventCache();
        notificationCache = CacheManager.getNotificationCache();
        genericCache = CacheManager.getGenericCache();
        userCache = CacheManager.getUserCache();
        this.bus = bus;
        this.userService = new UserService(bus);
    }

    public void handleNotification(Notification notification)
    {
        int notificationType = notification.getType();

        switch (notificationType)
        {
            case Notification.EVENT_CREATED:
                showCreateEventNotification(notification);
                break;
            case Notification.EVENT_INVITATION:
                showEventInvitedNotification(notification);
                break;
            case Notification.RSVP:
                showRSVPChangedNotification(notification);
                break;
            case Notification.EVENT_REMOVED:
                showEventRemovedNotification(notification);
                break;
            case Notification.EVENT_UPDATED:
                showEventUpdatedNotification(notification);
                break;
            case Notification.BLOCKED:
                handleBlockedNotification(notification);
                break;
            case Notification.UNBLOCKED:
                handleUnblockedNotification(notification);
                break;
            case Notification.FRIEND_RELOCATED:
                handleFriendNotification(notification);
                break;
            case Notification.NEW_FRIEND_ADDED:
                handleNewFriendJoinedAppNotification(notification);
                break;
            case Notification.CHAT:
                handleNewChatMessageNotification(notification);
                break;
        }
    }

    private void handleNewChatMessageNotification(final Notification notification) {

        notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {

                if (ifAppRunningInForeground())
                {
                    bus.post(new NewNotificationReceivedTrigger());

                } else
                {
                    buildNotification(notification, true);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    private void handleNewFriendJoinedAppNotification(final Notification notification)
    {
        notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
        {
            @Override
            public void onCompleted()
            {
                if (ifAppRunningInForeground())
                {
                    bus.post(new NewNotificationReceivedTrigger());

                } else
                {
                    buildNotification(notification, true);
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

    private void handleFriendNotification(Notification notification)
    {
        userCache.deleteFriends();
    }

    private void handleUnblockedNotification(Notification notification)
    {
        userCache.deleteFriends();
    }

    private void handleBlockedNotification(Notification notification)
    {
        userCache.deleteFriends();
    }

    private void showEventUpdatedNotification(final Notification notification)
    {
        fetchEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>()
        {
            @Override
            public void onCompleted()
            {
                if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId())))
                {
                    boolean isLocationUpdated = Boolean.parseBoolean(notification.getArgs().get("is_location_updated"));
                    boolean isTimeUpdated = Boolean.parseBoolean(notification.getArgs().get("is_time_updated"));

                    if (isLocationUpdated)
                    {
                        if (isTimeUpdated)
                        {
                            notification.setMessage(notification.getArgs().get("user_name") + "updated " + notification.getArgs().get("event_name"));
                        } else
                        {
                            notification.setMessage(notification.getArgs().get("user_name") + " updated the location for " + notification.getArgs().get("event_name"));
                        }
                    } else
                    {
                        if (isTimeUpdated)
                        {
                            notification.setMessage(notification.getArgs().get("user_name") + " updated the timings for " + notification.getArgs().get("event_name"));
                        }
                    }

                    notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
                    {
                        @Override
                        public void onCompleted()
                        {
                            if (ifAppRunningInForeground())
                            {
                                bus.post(new NewNotificationReceivedTrigger());

                            } else
                            {
                                buildNotification(notification, true);
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

    private void showEventRemovedNotification(final Notification notification)
    {
        eventCache.delete(notification.getEventId());

        if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId())))
        {
            notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
            {
                @Override
                public void onCompleted()
                {
                    if (ifAppRunningInForeground())
                    {
                        bus.post(new NewNotificationReceivedTrigger());

                    } else
                    {
                        buildNotification(notification, false);
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
    }

    private void showRSVPChangedNotification(final Notification notification)
    {
        eventCache.getEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>()
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
            public void onNext(Event event)
            {
                if (event == null)
                {
                    boolean shouldFetchEvent = false;
                    try
                    {
                        Event.Type eventType = Event.Type.valueOf(notification.getArgs().get("event_type"));
                        if (eventType == Event.Type.PUBLIC)
                        {
                            shouldFetchEvent = true;
                        }

                    } catch (Exception e)
                    {
                        Log.d("APP", "exception in notification service ---- can't convert to enum");
                    }

                    if (shouldFetchEvent)
                    {
                        fetchEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                notification.setMessage("New friends joined " + notification.getArgs().get("event_name"));
                                notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {
                                        if (ifAppRunningInForeground())
                                        {
                                            bus.post(new NewNotificationReceivedTrigger());

                                        } else
                                        {
                                            buildNotification(notification, true);
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
                } else
                {

                    notification.setMessage("New friends joined " + notification.getArgs().get("event_name"));
                    notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
                    {
                        @Override
                        public void onCompleted()
                        {
                            if (ifAppRunningInForeground())
                            {
                                bus.post(new NewNotificationReceivedTrigger());

                            } else
                            {
                                buildNotification(notification, true);
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
            }
        });
    }

    private void showEventInvitedNotification(final Notification notification)
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
                                            bus.post(new NewNotificationReceivedTrigger());

                                        } else
                                        {
                                            buildNotification(notification, true);
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
                                            bus.post(new NewNotificationReceivedTrigger());
                                        } else
                                        {
                                            buildNotification(notification, true);
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
        if (genericCache.get(CacheKeys.IS_APP_IN_FOREGROUND).equalsIgnoreCase("true"))
        {
            return true;
        } else
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
        notificationCache.getAll().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Notification>>()
        {
            @Override
            public void onCompleted()
            {
            }

            @Override
            public void onError(Throwable e)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.NOTIFICATIONS_FETCH_FAILURE, (Exception) e));
            }

            @Override
            public void onNext(List<Notification> notifications)
            {
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

    public void deleteNotificationFromCache(int notificationId)
    {
        notificationCache.clear(notificationId).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>()
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

    public void areNewNotificationsAvailable()
    {
        notificationCache.isAvaliable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>()
        {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {
                bus.post(new NewNotificationsAvailableTrigger());
            }

            @Override
            public void onNext(Boolean aBoolean)
            {
                if (aBoolean)
                {
                    bus.post(new NewNotificationsAvailableTrigger());
                } else
                {
                    bus.post(new NewNotificationsNotAvailableTrigger());
                }
            }
        });
    }

    private void buildNotification(Notification notification, boolean shouldGoToDetailsFragment)
    {
        Intent intent = new Intent(Reaper.getReaperContext(), LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (shouldGoToDetailsFragment)
        {
            intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "yes");
            intent.putExtra("event_id", notification.getEventId());
        } else
        {
            intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "no");
        }

        PendingIntent pendingIntent = PendingIntent
                .getActivity(Reaper.getReaperContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(Reaper.getReaperContext())
                .setSmallIcon(R.mipmap.logo_dark)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) Reaper.getReaperContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) (Math.random() * 1000), notificationBuilder.build());
    }
}
