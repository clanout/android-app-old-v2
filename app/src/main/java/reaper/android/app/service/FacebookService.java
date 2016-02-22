package reaper.android.app.service;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.squareup.otto.Bus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FacebookService
{
    private Bus bus;

    public FacebookService(Bus bus)
    {
        this.bus = bus;
    }

    public void getFacebookFriends(final boolean isPolling)
    {
        final GraphResponse[] response = new GraphResponse[1];
        final int[] totalFriends = {0};
        final List<String> friendsIdList = new ArrayList<>();

        Observable
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
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<String>>()
                           {
                               @Override
                               public void onCompleted()
                               {

                               }

                               @Override
                               public void onError(Throwable e)
                               {
                                   bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_FRIENDS_FETCHED_FAILURE, (Exception) e));
                               }

                               @Override
                               public void onNext(List<String> strings)
                               {
                                   bus.post(new FacebookFriendsIdFetchedTrigger(strings, isPolling));
                               }
                           }

                );
    }
}

