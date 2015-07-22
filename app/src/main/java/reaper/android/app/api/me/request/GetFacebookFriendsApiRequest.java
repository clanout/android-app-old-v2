package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class GetFacebookFriendsApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    public GetFacebookFriendsApiRequest(String zone)
    {
        this.zone = zone;
    }
}
