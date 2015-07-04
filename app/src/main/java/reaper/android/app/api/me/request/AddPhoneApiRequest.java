package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aditya on 02/07/15.
 */
public class AddPhoneApiRequest
{
    @SerializedName("phone_number")
    private String phoneNumber;

    public AddPhoneApiRequest(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
