package reaper.android.app.api.user.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api._core.ApiResponse;
import reaper.android.app.model.Friend;

public class GetFacebookFriendsApiResponse extends ApiResponse
{
    @SerializedName("local_friends")
    private List<Friend> friends;

    @SerializedName("other_friends")
    private List<Friend> otherFriends;

    public List<Friend> getFriends()
    {
        if (friends == null)
        {
            return new ArrayList<>();
        }
        else
        {
            return friends;
        }
    }

    public List<Friend> getOtherFriends()
    {
        if (otherFriends == null)
        {
            return new ArrayList<>();
        }
        else
        {
            return otherFriends;
        }
    }
}
