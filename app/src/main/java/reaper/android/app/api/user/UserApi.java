package reaper.android.app.api.user;

import reaper.android.app.api.user.request.BlockFriendsApiRequest;
import reaper.android.app.api.user.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.user.request.GetRegisteredContactsApiRequest;
import reaper.android.app.api.user.request.ShareFeedbackApiRequest;
import reaper.android.app.api.user.request.UpdateFacebookFriendsApiRequest;
import reaper.android.app.api.user.request.UpdateMobileAPiRequest;
import reaper.android.app.api.user.request.UpdateUserLocationApiRequest;
import reaper.android.app.api.user.response.GetFacebookFriendsApiResponse;
import reaper.android.app.api.user.response.GetRegisteredContactsApiResponse;
import reaper.android.app.api.user.response.UpdateUserLocationApiResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface UserApi
{
    @POST("/me/location")
    Observable<UpdateUserLocationApiResponse> updateUserLocation(@Body UpdateUserLocationApiRequest request);

    @POST("/me/phone")
    Observable<Response> updateMobile(@Body UpdateMobileAPiRequest request);

    @POST("/me/friends")
    Observable<GetFacebookFriendsApiResponse> getFacebookFriends(@Body GetFacebookFriendsApiRequest request);

    @POST("/me/contacts")
    Observable<GetRegisteredContactsApiResponse> getRegisteredContacts(@Body GetRegisteredContactsApiRequest request);

    @POST("/me/block")
    Observable<Response> blockFriends(@Body BlockFriendsApiRequest request);

    /* Old */
    @POST("/feedback")
    Observable<Response> shareFeedback(@Body ShareFeedbackApiRequest request);

    @POST("/me/add_friends")
    Observable<Response> updateFacebookFriends(@Body UpdateFacebookFriendsApiRequest request);
}
