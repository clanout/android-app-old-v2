package reaper.android.app.api.auth.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

/**
 * Created by Aditya on 27-08-2015.
 */
public class CreateNewSessionApiRequest extends ApiRequest
{
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("friend_list")
    private String friendList;

    public CreateNewSessionApiRequest(String accessToken, List<String> friendList)
    {
        this.accessToken = accessToken;
        this.friendList = GsonProvider.getGson().toJson(friendList);
    }
}
