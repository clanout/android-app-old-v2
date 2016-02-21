package reaper.android.app.cache.core;

import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.event.SQLiteEventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.generic.SQLiteGenericCache;
import reaper.android.app.cache.notification.NotificationCache;
import reaper.android.app.cache.notification.SQLiteNotificationCache;
import reaper.android.app.cache.user.SQLiteUserCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.CacheKeys;

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

    public static void clearAllCaches()
    {
        GenericCache genericCache = getGenericCache();
        UserCache userCache = getUserCache();
        EventCache eventCache = getEventCache();
        NotificationCache notificationCache = getNotificationCache();

        genericCache.delete(CacheKeys.ACTIVE_FRAGMENT);
        genericCache.delete(CacheKeys.GCM_TOKEN);
        genericCache.delete(CacheKeys.GCM_TOKEN_SENT_TO_SERVER);
        genericCache.delete(CacheKeys.LAST_UPDATE_TIMESTAMP);
        genericCache.delete(CacheKeys.SESSION_ID);
        genericCache.delete(CacheKeys.USER_ID);
        genericCache.delete(CacheKeys.USER_LOCATION);
        genericCache.delete(CacheKeys.USER_NAME);
        genericCache.delete(CacheKeys.USER_COVER_PIC);
        genericCache.delete(CacheKeys.USER_EMAIL);
        genericCache.delete(CacheKeys.USER_FIRST_NAME);
        genericCache.delete(CacheKeys.USER_LAST_NAME);
        genericCache.delete(CacheKeys.USER_GENDER);

        userCache.deleteFriends();
        userCache.deleteContacts();

        eventCache.deleteAll();

        notificationCache.clearAll();
    }
}
