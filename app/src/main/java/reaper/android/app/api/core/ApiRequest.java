package reaper.android.app.api.core;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.common.cache.Cache;

public abstract class ApiRequest
{
    @SerializedName("_SESSIONID")
    protected String sessionCookie;

    protected ApiRequest()
    {
        GenericCache cache = new GenericCache();
        this.sessionCookie = cache.get(CacheKeys.SESSION_ID);
    }
}
