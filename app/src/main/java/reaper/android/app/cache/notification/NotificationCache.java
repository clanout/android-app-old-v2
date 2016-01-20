package reaper.android.app.cache.notification;

import java.util.List;

import reaper.android.common.notification.Notification;
import rx.Observable;

public interface NotificationCache
{
    Observable<Object> put(Notification notification);

    Observable<List<Notification>> getAll();

    Observable<Object> clear();

    Observable<Object> markRead();

    Observable<Object> clear(int notificationId);

    Observable<Boolean> isAvaliable();

    void clearAll();
}
