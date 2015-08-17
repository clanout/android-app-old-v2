package reaper.android.app.api.me;

import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.BlockFriendsApiRequest;
import reaper.android.app.api.me.request.GetAllFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetPhoneContactsApiRequest;
import reaper.android.app.api.me.request.ShareFeedbackApiRequest;
import reaper.android.app.api.me.request.UserZoneUpdatedApiRequest;
import reaper.android.app.api.me.response.GetAllFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetPhoneContactsApiResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by aditya on 02/07/15.
 */
public interface MeApi
{
    @POST("/me/phone")
    Observable<Response> updatePhoneNumber(@Body AddPhoneApiRequest request);

    @POST("/me/friends")
    Observable<GetFacebookFriendsApiResponse> getFacebookFriends(@Body GetFacebookFriendsApiRequest request);

    @POST("/me/friends")
    Observable<GetAllFacebookFriendsApiResponse> getAllFacebookFriends(@Body GetAllFacebookFriendsApiRequest request);

    @POST("/me/contacts")
    Observable<GetPhoneContactsApiResponse> getPhoneContacts(@Body GetPhoneContactsApiRequest request);

    @POST("/me/block")
    Observable<Response> blockFriends(@Body BlockFriendsApiRequest request);

    @POST("/me/feedback")
    Observable<Response> shareFeedback(@Body ShareFeedbackApiRequest request);

    @POST("/me/location")
    Observable<Response> updateUserZone(@Body UserZoneUpdatedApiRequest request);
}
