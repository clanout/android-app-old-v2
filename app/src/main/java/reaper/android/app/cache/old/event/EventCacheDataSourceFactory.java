package reaper.android.app.cache.old.event;

import reaper.android.app.cache.old.event.sqlite.SQLiteEventCacheDataSource;

public class EventCacheDataSourceFactory
{
    public static EventCacheDataSource create()
    {
        return new SQLiteEventCacheDataSource();
    }
}
