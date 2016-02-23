package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class GetAppFriendsApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    public GetAppFriendsApiRequest(String zone)
    {
        this.zone = zone;
    }
}
