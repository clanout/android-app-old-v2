package reaper.android.app.api.auth.request;

import reaper.android.app.api.core.ApiRequest;

public class ValidateSessionApiRequest extends ApiRequest
{
    public ValidateSessionApiRequest(String sessionCookie)
    {
        this.sessionCookie = sessionCookie;
    }
}
