package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

import reaper.android.app.api.core.ApiRequest;
import reaper.android.app.api.core.GsonProvider;

public class GetPhoneContactsApiRequest extends ApiRequest
{
    @SerializedName("contacts")
    private String phoneNumberList;

    @SerializedName("zone")
    private String zone;

    public GetPhoneContactsApiRequest(Set<String> phoneNumberList, String zone)
    {
        this.phoneNumberList = GsonProvider.getGson().toJson(phoneNumberList);
        this.zone = zone;
    }
}
