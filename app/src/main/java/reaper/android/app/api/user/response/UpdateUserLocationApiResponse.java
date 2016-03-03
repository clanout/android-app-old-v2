package reaper.android.app.api.user.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class UpdateUserLocationApiResponse extends ApiRequest
{
    @SerializedName("is_relocated")
    private boolean isRelocated;

    public boolean isRelocated()
    {
        return isRelocated;
    }
}
