package reaper.android.app.cache._core;

import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.event.SQLiteEventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.generic.SQLiteGenericCache;
import reaper.android.app.cache.memory.MemoryCache;
import reaper.android.app.cache.memory.MemoryCacheImpl;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.cache.notification.SQLiteNotificationCache;
import reaper.android.app.cache.user.SQLiteUserCache;
import reaper.android.app.cache.user.UserCache;

public class CacheManager
{
    public static GenericCache getGenericCache()
    {
        return SQLiteGenericCache.getInstance();
    }

    public static EventCache getEventCache()
    {
        return SQLiteEventCache.getInstance();
    }

    public static UserCache getUserCache()
    {
        return SQLiteUserCache.getInstance();
    }

    public static NotificationCache getNotificationCache()
    {
        return SQLiteNotificationCache.getInstance();
    }

    public static MemoryCache getMemoryCache()
    {
        return MemoryCacheImpl.getInstance();
    }

    public static void clearAllCaches()
    {
        GenericCache genericCache = getGenericCache();
        UserCache userCache = getUserCache();
        EventCache eventCache = getEventCache();
        NotificationCache notificationCache = getNotificationCache();

        genericCache.deleteAll();

        userCache.deleteFriends();
        userCache.deleteContacts();

        eventCache.deleteAll();

        notificationCache.clearAll();
    }
}
