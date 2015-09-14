package reaper.android.app.cache.notification;

import reaper.android.common.notification.Notification;
import rx.Observable;

public interface NotificationCache
{
    Observable<Object> put(Notification notification);
}
