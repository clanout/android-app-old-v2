package reaper.android.app.api.me;

import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.BlockFriendsApiRequest;
import reaper.android.app.api.me.request.FetchPendingInvitesApiRequest;
import reaper.android.app.api.me.request.GetAllAppFriendsApiRequest;
import reaper.android.app.api.me.request.GetAppFriendsApiRequest;
import reaper.android.app.api.me.request.GetPhoneContactsApiRequest;
import reaper.android.app.api.me.request.ShareFeedbackApiRequest;
import reaper.android.app.api.me.request.UpdateFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.UserZoneUpdatedApiRequest;
import reaper.android.app.api.me.response.FetchPendingInvitesApiResponse;
import reaper.android.app.api.me.response.GetAllAppFriendsApiResponse;
import reaper.android.app.api.me.response.GetAppFriendsApiResponse;
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
    Observable<GetAppFriendsApiResponse> getAppFriends(@Body GetAppFriendsApiRequest request);

    @POST("/me/friends")
    Observable<GetAllAppFriendsApiResponse> getAllAppFriends(@Body GetAllAppFriendsApiRequest request);

    @POST("/me/contacts")
    Observable<GetPhoneContactsApiResponse> getPhoneContacts(@Body GetPhoneContactsApiRequest request);

    @POST("/me/block")
    Observable<Response> blockFriends(@Body BlockFriendsApiRequest request);

    @POST("/me/feedback")
    Observable<Response> shareFeedback(@Body ShareFeedbackApiRequest request);

    @POST("/me/location")
    Observable<Response> updateUserZone(@Body UserZoneUpdatedApiRequest request);

    @POST("/me/add_friends")
    Observable<Response> updateFacebookFriends(@Body UpdateFacebookFriendsApiRequest request);

    @POST("/me/fetch_pending_invites")
    Observable<FetchPendingInvitesApiResponse> fetchPendingInvites(@Body FetchPendingInvitesApiRequest request);
}
