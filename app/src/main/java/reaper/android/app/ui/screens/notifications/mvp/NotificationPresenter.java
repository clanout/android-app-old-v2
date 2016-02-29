package reaper.android.app.ui.screens.notifications.mvp;

import reaper.android.app.model.Notification;

public interface NotificationPresenter
{
    void attachView(NotificationView view);

    void detachView();

    void onNotificationSelected(Notification notification);

    void onNotificationDeleted(int position);

    void onDeleteAll();
}
