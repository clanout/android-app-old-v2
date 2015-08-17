package reaper.android.app.cache.generic;

import reaper.android.app.cache.generic.sqlite.SQLiteGenericCacheDataSource;

public class GenericCaheDataSourceFactory
{
    public static GenericCacheDataSource create()
    {
        return new SQLiteGenericCacheDataSource();
    }
}
