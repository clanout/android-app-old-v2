package reaper.android.app.api.user.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Friend;

public class GetRegisteredContactsApiResponse extends ApiResponse
{
    @SerializedName("registered_contacts")
    private List<Friend> registeredContacts;

    public List<Friend> getRegisteredContacts()
    {
        return registeredContacts;
    }
}
