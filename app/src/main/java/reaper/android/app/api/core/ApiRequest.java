package reaper.android.app.api.core;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;

public abstract class ApiRequest
{
    @SerializedName("_SESSIONID")
    protected String sessionCookie;

    protected ApiRequest()
    {
        GenericCache cache = CacheManager.getGenericCache();
        this.sessionCookie = cache.get(CacheKeys.SESSION_ID);
    }
}
