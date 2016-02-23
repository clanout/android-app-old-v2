package reaper.android.app.api._core;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.service.UserService;

public abstract class ApiRequest
{
    @SerializedName("_SESSIONID")
    protected String sessionCookie;

    protected ApiRequest()
    {
        this.sessionCookie = UserService.getInstance().getSessionId();
    }
}
