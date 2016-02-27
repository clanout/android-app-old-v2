package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

public class GetRegisteredContactsApiRequest extends ApiRequest
{
    @SerializedName("contacts")
    private String allContacts;

    @SerializedName("zone")
    private String zone;

    public GetRegisteredContactsApiRequest(List<String> allContacts, String zone)
    {
        this.allContacts = GsonProvider.getGson().toJson(allContacts);
        this.zone = zone;
    }
}
