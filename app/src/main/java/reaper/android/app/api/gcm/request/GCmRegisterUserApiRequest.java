package reaper.android.app.api.gcm.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class GCmRegisterUserApiRequest extends ApiRequest
{
    @SerializedName("token")
    private String token;

    public GCmRegisterUserApiRequest(String token)
    {
        this.token = token;
    }
}
