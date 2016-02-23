package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

/**
 * Created by aditya on 02/07/15.
 */
public class AddPhoneApiRequest extends ApiRequest
{
    @SerializedName("phone")
    private String phoneNumber;

    public AddPhoneApiRequest(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
