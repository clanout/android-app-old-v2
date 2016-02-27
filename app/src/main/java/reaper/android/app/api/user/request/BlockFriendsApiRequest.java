package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

public class BlockFriendsApiRequest extends ApiRequest
{
    @SerializedName("blocked_users")
    private String blockedUsers;

    @SerializedName("unblocked_users")
    private String unblockedUsers;

    public BlockFriendsApiRequest(List<String> blockedUsers, List<String> unblockedUsers)
    {
        this.blockedUsers = GsonProvider.getGson().toJson(blockedUsers);
        this.unblockedUsers = GsonProvider.getGson().toJson(unblockedUsers);
    }
}
