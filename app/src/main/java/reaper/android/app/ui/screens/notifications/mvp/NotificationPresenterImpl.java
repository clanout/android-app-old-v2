package reaper.android.app.ui.screens.notifications.mvp;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.service.NotificationService;
import reaper.android.app.model.Notification;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class NotificationPresenterImpl implements NotificationPresenter
{
    private NotificationView view;
    private NotificationService notificationService;

    private List<Notification> notifications;

    private CompositeSubscription subscriptions;

    public NotificationPresenterImpl(NotificationService notificationService)
    {
        this.notificationService = notificationService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(NotificationView view)
    {
        this.view = view;

        notificationService.markAllNotificationsAsRead();
        fetchNotifications();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        this.view = null;
    }

    @Override
    public void onNotificationSelected(final Notification notification)
    {
        notificationService.deleteNotificationFromCache(notification.getId());

        final String eventId = notification.getEventId();
        if (eventId == null || eventId.isEmpty())
        {
            view.navigateToHomeScreen();
        }
        else if (notification.getType() == Notification.CHAT)
        {
            view.navigateToChatScreen(eventId);
        }
        else
        {
            view.navigateToDetailsScreen(eventId);
        }
    }

    @Override
    public void onNotificationDeleted(int position)
    {
        Notification deletedNotification = notifications.remove(position);
        if (notifications.isEmpty())
        {
            view.displayNoNotificationsMessage();
        }

        notificationService.deleteNotificationFromCache(deletedNotification.getId());
    }

    @Override
    public void onDeleteAll()
    {
        view.displayNoNotificationsMessage();
        notificationService.deleteAllNotificationsFromCache();
    }

    private void fetchNotifications()
    {
        view.showLoading();

        Subscription subscription =
                notificationService
                        .fetchNotifications()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Notification>>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                if (notifications == null || notifications.isEmpty())
                                {
                                    notifications = new ArrayList<Notification>();
                                }
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                view.displayNoNotificationsMessage();
                            }

                            @Override
                            public void onNext(List<Notification> notifications)
                            {
                                if (notifications.isEmpty())
                                {
                                    view.displayNoNotificationsMessage();
                                }
                                else
                                {
                                    NotificationPresenterImpl.this.notifications = notifications;
                                    view.displayNotifications(notifications);
                                }
                            }
                        });

        subscriptions.add(subscription);
    }
}
