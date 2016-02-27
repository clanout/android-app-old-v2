package reaper.android.app.api.user.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api._core.ApiRequest;

public class ShareFeedbackApiRequest extends ApiRequest
{
    @SerializedName("type")
    private int type;

    @SerializedName("comment")
    private String comment;

    public ShareFeedbackApiRequest(String comment, int type)
    {
        this.comment = comment;
        this.type = type;
    }
}
