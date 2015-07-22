package reaper.android.app.service;

import android.util.Log;

import com.squareup.otto.Bus;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.me.MeApi;
import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.me.response.GetFacebookFriendsApiResponse;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.FacebookFriendsFetchedTrigger;
import reaper.android.common.cache.Cache;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserService
{
    private Bus bus;
    private MeApi meApi;

    public UserService(Bus bus)
    {
        this.bus = bus;
    }

    public String getActiveUser()
    {
        return "9320369679";
    }

    public void updatePhoneNumber(final String phoneNumber)
    {
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        meApi.updatePhoneNumber(new AddPhoneApiRequest(phoneNumber), new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {
                Cache.getInstance().put(CacheKeys.MY_PHONE_NUMBER, phoneNumber);
            }

            @Override
            public void failure(RetrofitError error)
            {

            }
        });

    }

    public void getFacebookFriends(String zone)
    {
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        meApi.getFacebookFriends(new GetFacebookFriendsApiRequest(zone), new Callback<GetFacebookFriendsApiResponse>()
        {
            @Override
            public void success(GetFacebookFriendsApiResponse getFacebookFriendsApiResponse, Response response)
            {
                Log.d("reap3r", "here");
                bus.post(new FacebookFriendsFetchedTrigger(getFacebookFriendsApiResponse.getFriends()));
            }

            @Override
            public void failure(RetrofitError error)
            {
                Log.d("reap3r", "error : " + error.getMessage());
                bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_FRIENDS_FETCH_FAILURE, error));
            }
        });
    }

    public void getPhoneContacts(String zone)
    {

    }
}
