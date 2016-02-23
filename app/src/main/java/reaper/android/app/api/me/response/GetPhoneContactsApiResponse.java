package reaper.android.app.api.me.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Friend;

public class GetPhoneContactsApiResponse extends ApiResponse
{
    @SerializedName("registered_contacts")
    private List<Friend> phoneContacts;

    public List<Friend> getPhoneContacts()
    {
        return phoneContacts;
    }
}
