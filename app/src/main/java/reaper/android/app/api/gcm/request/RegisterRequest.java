package reaper.android.app.api.gcm.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest
{
    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private String userId;

    public RegisterRequest(String token, String userId)
    {
        this.token = token;
        this.userId = userId;
    }
}
