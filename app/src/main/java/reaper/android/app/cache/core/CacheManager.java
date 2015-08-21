package reaper.android.app.cache.core;

import reaper.android.app.cache.event.EventCache;
import reaper.android.app.cache.event.SQLiteEventCache;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.generic.SQLiteGenericCache;

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
}
