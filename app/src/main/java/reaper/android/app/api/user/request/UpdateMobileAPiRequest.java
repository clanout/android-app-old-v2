package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by aditya on 02/07/15.
 */
public class UpdateMobileAPiRequest extends ApiRequest
{
    @SerializedName("phone")
    private String phoneNumber;

    public UpdateMobileAPiRequest(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
