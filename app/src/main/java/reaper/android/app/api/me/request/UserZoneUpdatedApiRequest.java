package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class UserZoneUpdatedApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    public UserZoneUpdatedApiRequest(String zone)
    {
        this.zone = zone;
    }
}
