package reaper.android.app.api.auth.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aditya on 27-08-2015.
 */
public class CreateNewSessionApiResponse
{
    @SerializedName("_SESSIONID")
    private String sessionId;

    public CreateNewSessionApiResponse(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }
}
