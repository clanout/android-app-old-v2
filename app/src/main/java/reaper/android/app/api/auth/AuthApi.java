package reaper.android.app.api.auth;

import reaper.android.app.api.auth.request.CreateNewSessionApiRequest;
import reaper.android.app.api.auth.request.ValidateSessionApiRequest;
import reaper.android.app.api.auth.response.CreateNewSessionApiResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface AuthApi
{
    @POST("/auth/validate")
    Observable<Response> validateSession(@Body ValidateSessionApiRequest request);

    @POST("/auth")
    Observable<CreateNewSessionApiResponse> createNewSession(@Body CreateNewSessionApiRequest request);
}
