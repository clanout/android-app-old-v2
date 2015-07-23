package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

import reaper.android.app.api.core.ApiRequest;
import reaper.android.app.api.core.GsonProvider;

public class GetPhoneContactsApiRequest extends ApiRequest
{
    @SerializedName("contacts")
    private String phoneNumberList;

    public GetPhoneContactsApiRequest(Set<String> phoneNumberList)
    {
        this.phoneNumberList = GsonProvider.getGson().toJson(phoneNumberList);
    }
}
