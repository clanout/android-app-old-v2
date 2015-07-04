package reaper.android.app.api.me;

import reaper.android.app.api.me.request.AddPhoneApiRequest;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.Callback;

/**
 * Created by aditya on 02/07/15.
 */
public interface MeApi
{
    @POST("/me/add-phone")
    public void updatePhoneNumber(@Body AddPhoneApiRequest request, Callback<Response> callback);
}
