package reaper.android.app.api.me;

import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.BlockFriendsApiRequest;
import reaper.android.app.api.me.request.GetAllFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetPhoneContactsApiRequest;
import reaper.android.app.api.me.request.ShareFeedbackApiRequest;
import reaper.android.app.api.me.response.GetAllFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetPhoneContactsApiResponse;
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

    @POST("/me/friends")
    public void getFacebookFriends(@Body GetFacebookFriendsApiRequest request, Callback<GetFacebookFriendsApiResponse> callback);

    @POST("/me/friends")
    public void getAllFacebookFriends(@Body GetAllFacebookFriendsApiRequest request, Callback<GetAllFacebookFriendsApiResponse> callback);

    @POST("/me/contacts")
    public void getPhoneContacts(@Body GetPhoneContactsApiRequest request, Callback<GetPhoneContactsApiResponse> callback);

    @POST("/me/block")
    public void blockFriends(@Body BlockFriendsApiRequest request, Callback<Response> callback);

    @POST("/me/feedback")
    public void shareFeedback(@Body ShareFeedbackApiRequest request, Callback<Response> callback);
}
