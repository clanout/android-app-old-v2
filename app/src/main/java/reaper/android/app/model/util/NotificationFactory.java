package reaper.android.app.model.util;

import android.os.Bundle;

import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.model.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationFactory
{
    private static final String TITLE = "clanOut";
    private static NotificationCache notificationCache = CacheManager.getNotificationCache();

    private static Type TYPE = new TypeToken<Map<String, String>>()
    {
    }.getType();

    public static Observable<Notification> create(Bundle data)
    {
        try {
            String type = data.getString("type");
            Map<String, String> args = GsonProvider.getGson()
                    .fromJson(data.getString("parameters"), TYPE);

            int typeCode = NotificationHelper.getType(type);

            return buildNotification(typeCode, args);

        }
        catch (Exception e) {
            Timber.e("Unable to create notification [" + e.getMessage() + "]");
            throw new IllegalStateException("Exception in NotificationFactory.create()");
        }
    }

    private static Observable<Notification> buildNotification(int typeCode, Map<String, String>
            args)
    {
        switch (typeCode) {
            case Notification.CHAT:
                return buildChatNotification(args);

            case Notification.BLOCKED:
                return buildBlockedNotification(args);

            case Notification.UNBLOCKED:
                return buildUnblockedNotification(args);

            case Notification.EVENT_CREATED:
                return buildEventCreatedNotification(args);

            case Notification.NEW_FRIEND_ADDED:
                return buildNewFriendJoinedAppNotification(args);

            case Notification.EVENT_UPDATED:
                return buildNewEventUpdatedNotification(args);

            case Notification.RSVP:
                return buildNewRsvpUpdatedNotification(args);

            case Notification.FRIEND_RELOCATED:
                return buildFriendRelocatedNotification(args);

            case Notification.STATUS:
                return buildStatusNotification(args);

            case Notification.EVENT_REMOVED:
                return buildEventRemovedNotification(args);

            default:
                throw new IllegalArgumentException("Notification Type Invalid");
        }
    }

    private static Observable<Notification> buildEventRemovedNotification(final Map<String, String> args)
    {
        return notificationCache.getAllForEvent(args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .EVENT_REMOVED, args);
                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_REMOVED, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildStatusNotification(final Map<String, String> args)
    {
        return notificationCache.getAll(Notification.STATUS, args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .STATUS, args);
                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.STATUS, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildFriendRelocatedNotification(Map<String, String> args)

    {
        String message = NotificationHelper.getMessage(Notification.FRIEND_RELOCATED, args);
        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.FRIEND_RELOCATED, args, message)
        );
    }

    private static Observable<Notification> buildNewRsvpUpdatedNotification(final Map<String, String> args)

    {
        return notificationCache.getAll(Notification.RSVP, args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .RSVP, args);
                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.RSVP, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildNewEventUpdatedNotification(final Map<String,
            String> args)

    {
        return notificationCache.getAll(Notification.EVENT_UPDATED, args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .EVENT_UPDATED, args);
                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_UPDATED, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildNewFriendJoinedAppNotification(Map<String,
            String> args)
    {
        String message = NotificationHelper.getMessage(Notification.NEW_FRIEND_ADDED, args);
        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.NEW_FRIEND_ADDED,
                        args, message)
        );
    }

    private static Observable<Notification> buildEventCreatedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.EVENT_CREATED, args);
        return Observable.just(
                getNotificationObjectHavingEventInformation(Notification.EVENT_CREATED, args,
                        message)
        );
    }

    private static Observable<Notification> buildUnblockedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.UNBLOCKED, args);
        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.UNBLOCKED, args,
                        message)
        );
    }

    private static Observable<Notification> buildBlockedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.BLOCKED, args);
        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.BLOCKED, args, message)
        );

    }

    private static Observable<Notification> buildChatNotification(final Map<String, String> args)
    {
        return notificationCache
                .getAll(Notification.CHAT, args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {

                        String message = NotificationHelper.getMessage(Notification.CHAT, args);
                        Notification notification = new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                                .type(Notification.CHAT)
                                .title(args.get("event_name"))
                                .eventId(args.get("event_id"))
                                .eventName(args.get("event_name"))
                                .userId(args.get("user_id"))
                                .userName("")
                                .timestamp(DateTime.now())
                                .message(NotificationHelper.getMessage(Notification.CHAT, args))
                                .isNew(true)
                                .args(args)
                                .build();
                        return Observable.just(notification);

                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Notification getNotificationObjectHavingEventInformation(int typeCode,
                                                                            Map<String, String>
                                                                                    args,
                                                                            String message)
    {
        return new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                .type(typeCode)
                .title(args.get("event_name"))
                .eventId(args.get("event_id"))
                .eventName(args.get("event_name"))
                .userId(args.get("user_id"))
                .userName("user_name")
                .timestamp(DateTime.now())
                .message(NotificationHelper.getMessage(typeCode, args))
                .isNew(true)
                .args(args)
                .build();
    }

    private static Notification getNotificationObjectNotHavingEventInformation(int typeCode,
                                                                               Map<String, String>
                                                                                       args,
                                                                               String message)
    {
        return new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                .type(typeCode)
                .title(TITLE)
                .eventId("")
                .eventName("")
                .userId("")
                .userName("")
                .timestamp(DateTime.now())
                .message(NotificationHelper.getMessage(typeCode, args))
                .isNew(true)
                .args(args)
                .build();
    }

//    private void test()
//    {
//        if ((typeCode == Notification.BLOCKED) || (typeCode == Notification.UNBLOCKED) ||
//                (typeCode == Notification.FRIEND_RELOCATED) || (typeCode == Notification
//                .NEW_FRIEND_ADDED)) {
//            Notification notification =
//                    new Notification.Builder(Integer.parseInt(args.get("notification_id")))
//                            .type(typeCode)
//                            .title(TITLE)
//                            .eventId("")
//                            .eventName("")
//                            .userId("")
//                            .userName("")
//                            .timestamp(DateTime.now())
//                            .message(NotificationHelper.getMessage(typeCode, args))
//                            .isNew(true)
//                            .args(args)
//                            .build();
//            return notification;
//        }
//        else if (typeCode == Notification.CHAT) {
//            Notification notification =
//                    new Notification.Builder(Integer.parseInt(args.get("notification_id")))
//                            .type(typeCode)
//                            .title(args.get("event_name"))
//                            .eventId(args.get("event_id"))
//                            .eventName(args.get("event_name"))
//                            .userId(args.get("user_id"))
//                            .userName("")
//                            .timestamp(DateTime.now())
//                            .message(NotificationHelper.getMessage(typeCode, args))
//                            .isNew(true)
//                            .args(args)
//                            .build();
//            return notification;
//        }
//        else {
//            Notification notification =
//                    new Notification.Builder(Integer.parseInt(args.get("notification_id")))
//                            .type(typeCode)
//                            .title(args.get("event_name"))
//                            .eventId(args.get("event_id"))
//                            .eventName(args.get("event_name"))
//                            .userId(args.get("user_id"))
//                            .userName(args.get("user_name"))
//                            .timestamp(DateTime.now())
//                            .message(NotificationHelper.getMessage(typeCode, args))
//                            .isNew(true)
//                            .args(args)
//                            .build();
//            return notification;
//        }
//
//    }
}
