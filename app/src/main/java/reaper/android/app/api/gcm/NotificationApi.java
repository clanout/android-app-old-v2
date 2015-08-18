package reaper.android.app.api.gcm;

import reaper.android.app.api.gcm.request.GCmRegisterUserApiRequest;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface NotificationApi
{
    @POST("/notification/register")
    Observable<Response> registerUser(@Body GCmRegisterUserApiRequest request);
}
