package reaper.android.app.api.auth.response;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiResponse;

/**
 * Created by Aditya on 27-08-2015.
 */
public class CreateNewSessionApiResponse extends ApiResponse
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
