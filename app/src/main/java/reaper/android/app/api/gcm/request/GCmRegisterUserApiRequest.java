package reaper.android.app.api.gcm.request;

import com.google.gson.annotations.SerializedName;

public class GCmRegisterUserApiRequest
{
    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private String userId;

    public GCmRegisterUserApiRequest(String token, String userId)
    {
        this.token = token;
        this.userId = userId;
    }
}
