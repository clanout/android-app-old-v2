package reaper.android.app.cache.notification;

import java.util.List;

import reaper.android.app.model.Notification;
import rx.Observable;

public interface NotificationCache
{
    Observable<Object> put(Notification notification);

    Observable<List<Notification>> getAll();

    Observable<Object> clear();

    Observable<Object> markRead();

    Observable<Object> clear(int notificationId);

    Observable<Boolean> isAvaliable();

    Observable<List<Notification>> getAllForType(int type);

    Observable<List<Notification>> getAllForEvent(String eventId);

    Observable<List<Notification>> getAll(int type, String eventId);

    Observable<Boolean> clear(List<Integer> notifificationIds);

    void clearAll();
}
