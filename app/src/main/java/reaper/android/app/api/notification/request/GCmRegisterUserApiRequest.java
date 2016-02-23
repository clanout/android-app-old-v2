package reaper.android.app.api.notification.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class GCmRegisterUserApiRequest extends ApiRequest
{
    @SerializedName("token")
    private String token;

    public GCmRegisterUserApiRequest(String token)
    {
        this.token = token;
    }
}
