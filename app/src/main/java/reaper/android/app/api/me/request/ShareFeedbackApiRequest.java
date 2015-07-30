package reaper.android.app.api.me.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class ShareFeedbackApiRequest extends ApiRequest
{
    @SerializedName("rating")
    private String rating;

    @SerializedName("comment")
    private String comment;

    public ShareFeedbackApiRequest(String comment, String rating)
    {
        this.comment = comment;
        this.rating = rating;
    }
}
