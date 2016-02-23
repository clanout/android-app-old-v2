package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

public class InviteUsersApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("user_id_list")
    private String userIdList;

    public InviteUsersApiRequest(String eventId, List<String> userIdList)
    {
        this.eventId = eventId;
        this.userIdList = GsonProvider.getGson().toJson(userIdList);
    }
}
