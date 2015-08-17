package reaper.android.app.cache.user;

import reaper.android.app.cache.user.sqlite.SQLiteUserCacheDataSource;

public class UserCacheDataSourceFactory
{
    public static UserCacheDataSource create()
    {
        return new SQLiteUserCacheDataSource();
    }
}
