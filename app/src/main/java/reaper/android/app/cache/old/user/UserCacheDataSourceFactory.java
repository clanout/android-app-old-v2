package reaper.android.app.cache.old.user;

import reaper.android.app.cache.old.user.sqlite.SQLiteUserCacheDataSource;

public class UserCacheDataSourceFactory
{
    public static UserCacheDataSource create()
    {
        return new SQLiteUserCacheDataSource();
    }
}
