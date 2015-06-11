package reaper.android.app.api.core;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.config.CacheKeys;
import reaper.android.common.cache.Cache;

public abstract class ApiRequest
{
    @SerializedName("_SESSIONID")
    protected String sessionCookie;

    protected ApiRequest()
    {
        this.sessionCookie = (String) Cache.getInstance().get(CacheKeys.SESSION_COOKIE);
    }
}
