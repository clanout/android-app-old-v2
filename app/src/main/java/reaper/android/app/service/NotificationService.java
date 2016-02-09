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

import org.joda.time.DateTime;

import java.util.ArrayList;
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
public class NotificationService {
    private EventApi eventApi;
    private EventCache eventCache;
    private NotificationCache notificationCache;
    private GenericCache genericCache;
    private UserCache userCache;
    private UserService userService;
    private Bus bus;

    public NotificationService(Bus bus) {
        this.eventApi = ApiManager.getInstance().getApi(EventApi.class);
        eventCache = CacheManager.getEventCache();
        notificationCache = CacheManager.getNotificationCache();
        genericCache = CacheManager.getGenericCache();
        userCache = CacheManager.getUserCache();
        this.bus = bus;
        this.userService = new UserService(bus);
    }

    public void handleNotification(Notification notification) {
        int notificationType = notification.getType();

        switch (notificationType) {
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
            case Notification.STATUS:
                handleNewStatusUpdateNotification(notification);
                break;
        }
    }

    private void handleNewStatusUpdateNotification(final Notification notification) {

        if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId()))) {
            notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                @Override
                public void onCompleted() {

                    if (ifAppRunningInForeground()) {
                        bus.post(new NewNotificationReceivedTrigger());

                    } else {
                        buildNotification(notification, true, false);
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
    }

    private void handleNewChatMessageNotification(final Notification notification) {

        if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId()))) {
            notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                @Override
                public void onCompleted() {

                    if (ifAppRunningInForeground()) {
                        bus.post(new NewNotificationReceivedTrigger());

                    } else {
                        buildNotification(notification, true, true);
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
    }

    private void handleNewFriendJoinedAppNotification(final Notification notification) {
        notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                if (ifAppRunningInForeground()) {
                    bus.post(new NewNotificationReceivedTrigger());

                } else {
                    buildNotification(notification, true, false);
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

    private void handleFriendNotification(Notification notification) {
        userCache.deleteFriends();
    }

    private void handleUnblockedNotification(Notification notification) {
        userCache.deleteFriends();
    }

    private void handleBlockedNotification(Notification notification) {
        userCache.deleteFriends();
    }

    private void showEventUpdatedNotification(final Notification notification) {
        fetchEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>() {
            @Override
            public void onCompleted() {
                if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId()))) {
                    boolean isLocationUpdated = Boolean.parseBoolean(notification.getArgs().get("is_location_updated"));
                    boolean isTimeUpdated = Boolean.parseBoolean(notification.getArgs().get("is_time_updated"));

                    if (isLocationUpdated) {
                        if (isTimeUpdated) {
                            notification.setMessage(notification.getArgs().get("user_name") + " updated " + notification.getArgs().get("event_name"));
                        } else {
                            notification.setMessage(notification.getArgs().get("user_name") + " updated the location for " + notification.getArgs().get("event_name"));
                        }
                    } else {
                        if (isTimeUpdated) {
                            notification.setMessage(notification.getArgs().get("user_name") + " updated the timings for " + notification.getArgs().get("event_name"));
                        }
                    }

                    notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                            if (ifAppRunningInForeground()) {
                                bus.post(new NewNotificationReceivedTrigger());

                            } else {
                                buildNotification(notification, true, false);
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
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Event event) {
                eventCache.save(event);
            }
        });
    }

    private void showEventRemovedNotification(final Notification notification) {
        eventCache.delete(notification.getEventId());

        if (!(notification.getArgs().get("user_id").equals(userService.getActiveUserId()))) {
            notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                @Override
                public void onCompleted() {
                    if (ifAppRunningInForeground()) {
                        bus.post(new NewNotificationReceivedTrigger());

                    } else {
                        buildNotification(notification, false, false);
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
    }

    private void showRSVPChangedNotification(final Notification notification) {
        eventCache.getEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Event event) {
                if (event == null) {
                    boolean shouldFetchEvent = false;
                    try {
                        Event.Type eventType = Event.Type.valueOf(notification.getArgs().get("event_type"));
                        if (eventType == Event.Type.PUBLIC) {
                            shouldFetchEvent = true;
                        }

                    } catch (Exception e) {
                        Log.d("APP", "exception in notification service ---- can't convert to enum");
                    }

                    if (shouldFetchEvent) {
                        fetchEvent(notification.getEventId()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Event>() {
                            @Override
                            public void onCompleted() {
                                notification.setMessage("New friends joined " + notification.getArgs().get("event_name"));
                                notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new NewNotificationReceivedTrigger());

                                        } else {
                                            buildNotification(notification, true, false);
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

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Event event) {
                                eventCache.save(event);
                            }
                        });
                    }
                } else {

                    notification.setMessage("New friends joined " + notification.getArgs().get("event_name"));
                    notificationCache.put(notification).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                            if (ifAppRunningInForeground()) {
                                bus.post(new NewNotificationReceivedTrigger());

                            } else {
                                buildNotification(notification, true, false);
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
            }
        });
    }

    private void showEventInvitedNotification(final Notification notification) {
        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        notificationCache.put(notification)
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new NewNotificationReceivedTrigger());

                                        } else {
                                            buildNotification(notification, true, false);
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

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Event event) {
                        eventCache.save(event);
                    }
                });
    }

    private void showCreateEventNotification(final Notification notification) {

        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        notificationCache.put(notification)
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new NewNotificationReceivedTrigger());
                                        } else {
                                            buildNotification(notification, true, false);
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

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Event event) {
                        eventCache.save(event);
                    }
                });
    }

    private boolean ifAppRunningInForeground() {
        if (genericCache.get(CacheKeys.IS_APP_IN_FOREGROUND).equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    private Observable<Event> fetchEvent(String eventId) {
        return eventApi.fetchEvent(new FetchEventApiRequest(eventId))
                .map(new Func1<FetchEventApiResponse, Event>() {
                    @Override
                    public Event call(FetchEventApiResponse fetchEventApiResponse) {
                        return fetchEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void fetchAllNotifications() {
        notificationCache.getAll().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Notification>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                bus.post(new GenericErrorTrigger(ErrorCode.NOTIFICATIONS_FETCH_FAILURE, (Exception) e));
            }

            @Override
            public void onNext(final List<Notification> notifications) {

                eventCache.getEvents()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Event>>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<Event> events) {

                                List<Notification> filteredNotifications = new ArrayList<Notification>();

                                for (Notification notification : notifications) {
                                    String eventId = notification.getEventId();

                                    if (eventId == null || eventId.isEmpty()) {

                                        Log.d("APP", "event id empty");
                                        filteredNotifications.add(notification);
                                    } else {

                                        Event event = new Event();
                                        event.setId(eventId);

                                        if (events.contains(event)) {

                                            Log.d("APP", "contains");

                                            if (events.get(events.indexOf(event)).getEndTime().getMillis() > DateTime.now().getMillis()) {

                                                Log.d("APP", "adding");
                                                filteredNotifications.add(notification);
                                            }
                                        }
                                    }
                                }

                                Log.d("APP", "size --- " + filteredNotifications.size());
                                bus.post(new NotificationsFetchedTrigger(filteredNotifications));

                            }
                        });
            }
        });
    }

    public void deleteAllNotificationsFromCache() {
        notificationCache.clear().observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    public void markAllNotificationsAsRead() {
        notificationCache.markRead().observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    public void deleteNotificationFromCache(int notificationId) {
        notificationCache.clear(notificationId).observeOn(Schedulers.newThread()).subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    public void areNewNotificationsAvailable() {
        notificationCache.isAvaliable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                bus.post(new NewNotificationsAvailableTrigger());
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    bus.post(new NewNotificationsAvailableTrigger());
                } else {
                    bus.post(new NewNotificationsNotAvailableTrigger());
                }
            }
        });
    }

    private void buildNotification(final Notification notification, final boolean shouldGoToDetailsFragment, final boolean shouldGoToChatFragment) {
        final Intent intent = new Intent(Reaper.getReaperContext(), LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final int requestCode = ("someString" + System.currentTimeMillis()).hashCode();

        notificationCache.getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<List<Notification>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Log.d("APP", "onError build noti --- notificationCache.getAll()");
                    }

                    @Override
                    public void onNext(List<Notification> notifications) {

                        Log.d("APP", "onNext build noti --- notificationCache.getAll()" + notifications.size());


                        if (notifications.size() == 0) {

                        } else if (notifications.size() == 1) {

                            // if only one notification

                            if (shouldGoToDetailsFragment) {


                                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "yes");
                                intent.putExtra("event_id", notification.getEventId());
                            } else {


                                intent.putExtra(BundleKeys.SHOULD_GO_TO_DETAILS_FRAGMENT, "no");
                            }

                            if (shouldGoToChatFragment) {
                                intent.putExtra(BundleKeys.SHOULD_GO_TO_CHAT_FRAGMENT, "yes");
                                intent.putExtra(BundleKeys.EVENT_NAME, notification.getArgs().get("event_name"));
                            } else {
                                intent.putExtra(BundleKeys.SHOULD_GO_TO_CHAT_FRAGMENT, "no");
                            }

                        } else if (notifications.size() > 1) {

                            // more than one notification

                            intent.putExtra(BundleKeys.SHOULD_GO_TO_NOTIFICATION_FRAGMENT, "yes");
                        }


                        intent.putExtra("randomRequestCode", requestCode);

                        PendingIntent pendingIntent = PendingIntent
                                .getActivity(Reaper.getReaperContext(), requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(Reaper.getReaperContext())
                                .setSmallIcon(R.mipmap.logo_red)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                        // Set Title and message for merged view

                        if (notifications.size() == 1) {
                            notificationBuilder.setContentTitle(notification.getTitle());
                            notificationBuilder.setContentText(notification.getMessage());
                        } else if (notifications.size() > 1) {

                            notificationBuilder.setContentTitle("Clanout");

                            int chatCount = 0;
                            int updateCount = 0;
                            int invitationCount = 0;

                            for (Notification noti : notifications) {

                                if (noti.getType() == Notification.CHAT) {
                                    chatCount++;
                                } else if (noti.getType() == Notification.EVENT_INVITATION) {
                                    invitationCount++;
                                } else if (noti.getType() == Notification.EVENT_UPDATED || notification.getType() == Notification.EVENT_REMOVED) {
                                    updateCount++;
                                }
                            }

                            String message = "You have ";

                            if (invitationCount != 0) {

                                if (updateCount == 0 && chatCount == 0) {

                                    if (invitationCount == 1) {
                                        message = message + invitationCount + " invitation";
                                    } else {
                                        message = message + invitationCount + " invitations";
                                    }
                                } else {

                                    if (updateCount == 0 || chatCount == 0) {
                                        if (invitationCount == 1) {
                                            message = message + invitationCount + " invitation and ";
                                        } else {
                                            message = message + invitationCount + " invitations and ";
                                        }
                                    } else {

                                        if (invitationCount == 1) {
                                            message = message + invitationCount + " invitation, ";
                                        } else {
                                            message = message + invitationCount + " invitations, ";
                                        }
                                    }
                                }
                            }

                            if (updateCount != 0) {

                                if (chatCount == 0) {
                                    if (updateCount == 1) {
                                        message = message + updateCount + " clan update";
                                    } else {
                                        message = message + updateCount + " clan updates";
                                    }
                                } else {

                                    if (updateCount == 1) {
                                        message = message + updateCount + " clan update and ";
                                    } else {

                                        message = message + updateCount + " clan updates and ";
                                    }
                                }
                            }

                            if (chatCount != 0) {

                                if (chatCount == 1) {
                                    message = message + chatCount + " conversation";
                                } else {
                                    message = message + chatCount + " conversations";
                                }
                            }

                            if (chatCount == 0 && updateCount == 0 && invitationCount == 0) {

                                if (notifications.size() == 1) {
                                    message = "You have " + notifications.size() + " new notification";

                                } else {

                                    message = "You have " + notifications.size() + " new notifications";
                                }
                            }

                            notificationBuilder.setContentText(message);


                            // Set Expanded View

//                            NotificationCompat.InboxStyle inboxStyle =
//                                    new NotificationCompat.InboxStyle();
//
//                            inboxStyle.setBigContentTitle("Clanout");
//
//                            for (Notification noti : notifications) {
//                                if (noti.getType() == Notification.CHAT || noti.getType() == Notification.EVENT_INVITATION || noti.getType() == Notification.EVENT_UPDATED || noti.getType() == Notification.EVENT_REMOVED) {
//                                    inboxStyle.addLine(noti.getMessage());
//                                }
//                            }
//
//                            notificationBuilder.setStyle(inboxStyle);

                            StringBuilder bigTextMessage = new StringBuilder();

                            for (Notification noti : notifications) {
                                if (noti.getType() == Notification.CHAT || noti.getType() == Notification.EVENT_INVITATION || noti.getType() == Notification.EVENT_UPDATED || noti.getType() == Notification.EVENT_REMOVED) {

                                    bigTextMessage.append("\u25CF" + " " + noti.getMessage());
                                    bigTextMessage.append("\n");
                                }
                            }

                            if (bigTextMessage.toString().isEmpty()) {
                                for (Notification noti : notifications) {

                                    bigTextMessage.append("\u25CF" + " " + noti.getMessage());
                                    bigTextMessage.append("\n");

                                }
                            }


                            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextMessage.toString()));

                        }

                        NotificationManager notificationManager =
                                (NotificationManager) Reaper.getReaperContext().getSystemService(Context.NOTIFICATION_SERVICE);

//                notificationManager.notify((int) (Math.random() * 1000), notificationBuilder.build());

                        notificationManager.notify(1, notificationBuilder.build());
                    }
                });
    }
}
