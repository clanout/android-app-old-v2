package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api.core.ApiRequest;
import reaper.android.app.api.core.GsonProvider;

/**
 * Created by Aditya on 27-08-2015.
 */
public class UpdateFacebookFriendsApiRequest extends ApiRequest
{
    @SerializedName("friend_list")
    private String friendIdList;

    public UpdateFacebookFriendsApiRequest(List<String> friendIdList)
    {
        this.friendIdList = GsonProvider.getGson().toJson(friendIdList);
    }
}
