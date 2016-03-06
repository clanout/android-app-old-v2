package reaper.android.app.model.util;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.config.NotificationMessages;
import reaper.android.app.model.Notification;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
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

            case Notification.EVENT_INVITATION:
                return buildEventInvitationNotification(args);

            default:
                throw new IllegalArgumentException("Notification Type Invalid");
        }
    }

    private static Observable<Notification> buildEventInvitationNotification(final Map<String,
            String>
                                                                                     args)
    {
        Observable<List<Notification>> getAllInvitationsForEvent = notificationCache.getAll
                (Notification.EVENT_INVITATION, args.get("event_id"));
        Observable<List<Notification>> getCreateNotificationsForEvent = notificationCache.getAll
                (Notification.EVENT_CREATED, args.get("event_id"));

        return Observable
                .zip(getAllInvitationsForEvent,
                        getCreateNotificationsForEvent,
                        new Func2<List<Notification>, List<Notification>,
                                Pair<List<Notification>, List<Notification>>>()
                        {
                            @Override
                            public Pair<List<Notification>, List<Notification>> call
                                    (List<Notification> inviteNotifs,
                                     List<Notification> createNotifs)
                            {
                                return new Pair<>(inviteNotifs, createNotifs);
                            }
                        })
                .flatMap(new Func1<Pair<List<Notification>, List<Notification>>,
                        Observable<List<Notification>>>()
                {
                    @Override
                    public Observable<List<Notification>> call(final Pair<List<Notification>,
                            List<Notification>> notifs)
                    {
                        List<Integer> createNotificationIds = getNotificationIdsList(notifs.second);
                        return notificationCache
                                .clear(createNotificationIds)
                                .flatMap(new Func1<Boolean, Observable<List<Notification>>>()
                                {
                                    @Override
                                    public Observable<List<Notification>> call(Boolean aBoolean)
                                    {
                                        return Observable.just(notifs.first);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<List<Notification>, Observable<Integer>>()
                         {
                             @Override
                             public Observable<Integer> call(final List<Notification> notifications)
                             {
                                 List<Integer> notificationIds = getNotificationIdsList
                                         (notifications);
                                 return notificationCache
                                         .clear(notificationIds)
                                         .flatMap(new Func1<Boolean, Observable<Integer>>()
                                         {
                                             @Override
                                             public Observable<Integer> call(Boolean aBoolean)
                                             {
                                                 return calculatePreviousInviteeCount
                                                         (notifications);
                                             }
                                         });
                             }
                         }
                )
                .flatMap(new Func1<Integer, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Integer previousInviteeCount)
                    {

                        String message = getInviteNotificationMessage(previousInviteeCount, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_INVITATION, args, message);

                        return Observable.just(notification);
                    }
                });
    }

    private static Observable<Notification> buildEventRemovedNotification(final Map<String,
            String> args)
    {
        return notificationCache.getAllForEvent(args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(final List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

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
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

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

    private static Observable<Notification> buildFriendRelocatedNotification(Map<String, String>
                                                                                     args)

    {
        String message = NotificationHelper.getMessage(Notification.FRIEND_RELOCATED, args);

        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.FRIEND_RELOCATED,
                        args, message)
        );
    }

    private static Observable<Notification> buildNewRsvpUpdatedNotification(final Map<String,
            String> args)

    {
        return notificationCache.getAll(Notification.RSVP, args.get("event_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

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
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

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
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {

                        String message = NotificationHelper.getMessage(Notification.CHAT, args);
                        Notification notification = new Notification.Builder(Integer.parseInt
                                (args.get("notification_id")))
                                .type(Notification.CHAT)
                                .title(args.get("event_name"))
                                .eventId(args.get("event_id"))
                                .eventName(args.get("event_name"))
                                .userId(args.get("user_id"))
                                .userName("")
                                .timestamp(DateTime.now())
                                .message(message)
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
                .message(message)
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
                .message(message)
                .isNew(true)
                .args(args)
                .build();
    }

    private static String getInviteNotificationMessage(Integer previousInviteeCount, Map<String,
            String> args)
    {
        String message = "";

        if (previousInviteeCount == 0) {
            message = NotificationHelper.getMessage(Notification
                    .EVENT_INVITATION, args);

        }
        else {

            message = String.format(NotificationMessages.EVENT_INVITATION,
                    args.get
                            ("user_name") + " and " + previousInviteeCount + " others",
                    args.get("event_name"));

        }

        return message;
    }

    private static Observable<Integer> calculatePreviousInviteeCount(List<Notification>
                                                                             notifications)
    {
        if (notifications.size() != 0) {

            String message = notifications.get(0).getMessage();

            return Observable.just(getInviteeCountFromMessage(message));

        }
        else {
            return Observable.just(0);
        }
    }

    private static List<Integer> getNotificationIdsList(List<Notification> notifications)
    {
        List<Integer> notificationIds = new ArrayList<Integer>();
        for (Notification notification : notifications) {
            notificationIds.add(notification.getId());
        }

        return notificationIds;
    }

    private static Integer getInviteeCountFromMessage(String message)
    {
        try {
            if (message.contains("others invited you to")) {

                String[] wordArray = message.split(" ");

                try {

                    return Integer.valueOf(wordArray[3]) + 1;
                }
                catch (Exception e) {
                    try {
                        return Integer.valueOf(wordArray[2]) + 1;
                    }
                    catch (Exception exception) {
                        return 0;
                    }
                }

            }
            else if (message.contains("invited you to")) {

                return 1;

            }
            else {

                return 0;
            }
        }
        catch (Exception e) {
            return 0;
        }

    }

}
