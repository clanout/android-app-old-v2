package reaper.android.app.cache.event;

import reaper.android.app.cache.event.sqlite.SQLiteEventCacheDataSource;

public class EventCacheDataSourceFactory
{
    public static EventCacheDataSource create()
    {
        return new SQLiteEventCacheDataSource();
    }
}
