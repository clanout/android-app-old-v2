package reaper.android.app.api.fb;

import reaper.android.app.api.fb.response.FacebookCoverPicResponse;
import reaper.android.app.api.fb.response.FacebookProfileResponse;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by Aditya on 27-08-2015.
 */
public interface FacebookApi
{
    @GET("/me?fields=first_name,last_name,gender,email")
    Observable<FacebookProfileResponse> getProfile();

    @GET("/me?fields=cover")
    Observable<FacebookCoverPicResponse> getCoverPic();
}
