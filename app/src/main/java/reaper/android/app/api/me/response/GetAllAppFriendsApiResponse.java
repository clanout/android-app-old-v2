package reaper.android.app.api.me.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api.core.ApiResponse;
import reaper.android.app.model.Friend;

public class GetAllAppFriendsApiResponse extends ApiResponse
{
    @SerializedName("friends")
    private List<Friend> friends;

    public List<Friend> getFriends()
    {
        return friends;
    }
}
