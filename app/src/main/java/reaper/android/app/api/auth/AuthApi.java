package reaper.android.app.api.auth;

import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthApi
{
    @POST("/auth/validate")
    public void validateSession(@Body ValidateSessionApiRequest request, Callback<Response> callback);
}
