package reaper.android.app.service;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.squareup.otto.Bus;

import org.json.JSONArray;

import reaper.android.app.api.core.FacebookApiManager;
import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.api.fb.FacebookApi;
import reaper.android.app.api.fb.response.FacebookProfileResponse;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookProfileFetchedTrigger;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Aditya on 23-08-2015.
 */
public class FacebookService
{
    private Bus bus;

    public FacebookService(Bus bus)
    {
        this.bus = bus;
    }

    public void getUserProfile()
    {
        FacebookApi facebookApi = FacebookApiManager.getInstance().getApi();
        facebookApi.getProfile()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<FacebookProfileResponse>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d("FacebookApi", "COMPLETED");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_PROFILE_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(FacebookProfileResponse response)
                    {
                        bus.post(new FacebookProfileFetchedTrigger(response.getId(), response.getFirstname(), response.getLastname(), response.getGender(), response.getEmail()));
                    }
                });
    }

    public void getUserFriends()
    {
        GraphRequest graphRequest = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback()
        {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse)
            {

                Log.d("APP", "graphResponse ------ " + graphResponse);
            }
        });
        graphRequest.executeAsync();
    }
}
