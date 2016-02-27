package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class GetFacebookFriendsApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    public GetFacebookFriendsApiRequest(String zone)
    {
        this.zone = zone;
    }
}
