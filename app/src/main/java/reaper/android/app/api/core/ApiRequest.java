package reaper.android.app.api.core;

import com.google.gson.annotations.SerializedName;

public abstract class ApiRequest
{
    @SerializedName("_URI")
    protected String uri;

    @SerializedName("_ME")
    protected String activeUser;

    protected ApiRequest(String uri)
    {
        this.uri = uri;
        this.activeUser = "9320369679";
    }
}
