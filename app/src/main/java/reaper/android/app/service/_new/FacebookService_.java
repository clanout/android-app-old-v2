package reaper.android.app.service._new;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import reaper.android.app.api.core.FacebookApiManager;
import reaper.android.app.api.fb.FacebookApi;
import reaper.android.app.api.fb.response.FacebookCoverPicResponse;
import reaper.android.app.api.fb.response.FacebookProfileResponse;
import reaper.android.app.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class FacebookService_
{
    public static FacebookService_ instance;

    public static void init()
    {
        instance = new FacebookService_();
    }

    public static FacebookService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[FacebookService Not Initialized]");
        }

        return instance;
    }

    public static List<String> PERMISSIONS = Arrays.asList("email", "user_friends");
    private static final String PROFILE_PIC_URL = "https://graph.facebook.com/v2.4/$$$/picture?height=1000";

    private FacebookApi facebookApi;

    private FacebookService_()
    {
        facebookApi = FacebookApiManager.getInstance().getApi();
    }

    public boolean isAccessTokenValid()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken == null)
        {
            return false;
        }

        if (accessToken.isExpired())
        {
            return false;
        }

        return accessToken.getDeclinedPermissions().size() == 0;

    }

    public void logout()
    {
        LoginManager.getInstance().logOut();
    }

    public String getAccessToken()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
        {
            return null;
        }

        return accessToken.getToken();
    }

    public Set<String> getDeclinedPermissions()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
        {
            return null;
        }

        return accessToken.getDeclinedPermissions();
    }

    public Observable<User> getUser()
    {
        return Observable
                .zip(getProfile(), getCoverPicUrl(), new Func2<FacebookProfileResponse, String, User>()
                {
                    @Override
                    public User call(FacebookProfileResponse profile, String coverPicUrl)
                    {
                        User user = new User();

                        user.setId(profile.getId());
                        user.setFirstname(profile.getFirstname());
                        user.setLastname(profile.getLastname());
                        user.setEmail(profile.getEmail());
                        user.setGender(profile.getGender());

                        String profilePicUrl = PROFILE_PIC_URL.replace("$$$", user.getId());
                        user.setProfilePicUrl(profilePicUrl);

                        user.setCoverPicUrl(coverPicUrl);

                        return user;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<String>> getFriends()
    {
        final GraphResponse[] response = new GraphResponse[1];
        final int[] totalFriends = {0};
        final List<String> friendsIdList = new ArrayList<>();

        return Observable
                .create(new Observable.OnSubscribe<List<String>>()
                        {
                            @Override
                            public void call(final Subscriber<? super List<String>> subscriber)
                            {
                                GraphRequest graphRequest =
                                        GraphRequest.newMyFriendsRequest(AccessToken
                                                        .getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback()
                                                {
                                                    @Override
                                                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse)
                                                    {
                                                        response[0] = graphResponse;
                                                        friendsIdList.clear();
                                                        JSONObject jsonObject = response[0].getJSONObject();
                                                        totalFriends[0] = 0;

                                                        try
                                                        {
                                                            JSONArray jsonArrayData = jsonObject
                                                                    .getJSONArray("data");
                                                            JSONObject dataObject;
                                                            for (int j = 0; j < jsonArrayData.length(); j++)
                                                            {

                                                                dataObject = (JSONObject) jsonArrayData
                                                                        .get(j);
                                                                friendsIdList
                                                                        .add(dataObject.getString("id"));
                                                            }

                                                            JSONObject jsonObjectSummary = jsonObject
                                                                    .getJSONObject("summary");
                                                            totalFriends[0] = Integer
                                                                    .parseInt(jsonObjectSummary
                                                                            .getString("total_count"));
                                                        }
                                                        catch (Exception e)
                                                        {
                                                            subscriber.onNext(null);
                                                            subscriber.onCompleted();
                                                            totalFriends[0] = -1;
                                                            return;
                                                        }

                                                        int count = (totalFriends[0] / 25) + 2;

                                                        for (int i = 0; i < count; i++)
                                                        {
                                                            GraphRequest nextPageRequest = response[0]
                                                                    .getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                                                            if (nextPageRequest != null)
                                                            {
                                                                nextPageRequest
                                                                        .setCallback(new GraphRequest.Callback()
                                                                        {
                                                                            @Override
                                                                            public void onCompleted(GraphResponse graphResponse)
                                                                            {
                                                                                response[0] = graphResponse;
                                                                                JSONObject responseObject = response[0]
                                                                                        .getJSONObject();

                                                                                try
                                                                                {
                                                                                    JSONArray jsonArrayData = responseObject
                                                                                            .getJSONArray("data");
                                                                                    JSONObject dataObject;
                                                                                    for (int j = 0; j < jsonArrayData
                                                                                            .length(); j++)
                                                                                    {

                                                                                        dataObject = (JSONObject) jsonArrayData
                                                                                                .get(j);
                                                                                        friendsIdList
                                                                                                .add(dataObject
                                                                                                        .getString("id"));
                                                                                    }
                                                                                }
                                                                                catch (Exception e)
                                                                                {
                                                                                    subscriber.onNext(null);
                                                                                    subscriber
                                                                                            .onCompleted();
                                                                                    totalFriends[0] = -1;
                                                                                    return;
                                                                                }

                                                                            }
                                                                        });

                                                                nextPageRequest.executeAndWait();
                                                            }
                                                            else
                                                            {
                                                                break;
                                                            }
                                                        }

                                                    }
                                                }
                                        );

                                graphRequest.executeAndWait();

                                if (totalFriends[0] != -1)
                                {
                                    subscriber.onNext(friendsIdList);
                                    subscriber.onCompleted();
                                }
                            }
                        }

                )
                .subscribeOn(Schedulers.newThread());
    }


    /* Helper Methods */
    private Observable<String> getCoverPicUrl()
    {
        return facebookApi
                .getCoverPic()
                .map(new Func1<FacebookCoverPicResponse, String>()
                {
                    @Override
                    public String call(FacebookCoverPicResponse response)
                    {
                        return response.getCover().getSource();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<FacebookProfileResponse> getProfile()
    {
        return facebookApi.getProfile()
                          .subscribeOn(Schedulers.newThread());
    }
}

