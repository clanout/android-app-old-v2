package reaper.android.app.service;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.squareup.otto.Bus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.core.FacebookApiManager;
import reaper.android.app.api.fb.FacebookApi;
import reaper.android.app.api.fb.response.FacebookCoverPicResponse;
import reaper.android.app.api.fb.response.FacebookProfileResponse;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.facebook.FacebookCoverPicFetchedTrigger;
import reaper.android.app.trigger.facebook.FacebookFriendsIdFetchedTrigger;
import reaper.android.app.trigger.facebook.FacebookProfileFetchedTrigger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Aditya on 23-08-2015.
 */
public class FacebookService
{
    private Bus bus;
    private FacebookApi facebookApi;

    private GraphResponse response;
    private int totalFriends = 0;
    private List<String> friendsIdList;

    public FacebookService(Bus bus)
    {
        this.bus = bus;
        facebookApi = FacebookApiManager.getInstance().getApi();
        friendsIdList = new ArrayList<>();
    }

    public void getUserCoverPic()
    {
        facebookApi.getCoverPic().subscribeOn(Schedulers.newThread())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Subscriber<FacebookCoverPicResponse>()
                   {
                       @Override
                       public void onCompleted()
                       {

                       }

                       @Override
                       public void onError(Throwable e)
                       {

                       }

                       @Override
                       public void onNext(FacebookCoverPicResponse response)
                       {

                           bus.post(new FacebookCoverPicFetchedTrigger(response.getCover()
                                                                               .getSource()));
                       }
                   });
    }


    public void getUserFacebookProfile()
    {
        facebookApi.getProfile()
                   .subscribeOn(Schedulers.newThread())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Subscriber<FacebookProfileResponse>()
                   {
                       @Override
                       public void onCompleted()
                       {
                       }

                       @Override
                       public void onError(Throwable e)
                       {
                           bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_PROFILE_FETCH_FAILURE, (Exception) e));
                       }

                       @Override
                       public void onNext(FacebookProfileResponse response)
                       {
                           bus.post(new FacebookProfileFetchedTrigger(response.getId(), response
                                   .getFirstname(), response.getLastname(), response
                                   .getGender(), response.getEmail()));
                       }
                   });
    }

    public void getFacebookFriends(final boolean isPolling)
    {
        Observable.create(new Observable.OnSubscribe<List<String>>()
                          {
                              @Override
                              public void call(final Subscriber<? super List<String>> subscriber)
                              {
                                  GraphRequest graphRequest = GraphRequest.newMyFriendsRequest(AccessToken
                                                  .getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback()
                                          {
                                              @Override
                                              public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse)
                                              {
                                                  response = graphResponse;
                                                  friendsIdList = new ArrayList<String>();
                                                  JSONObject jsonObject = response.getJSONObject();
                                                  totalFriends = 0;

                                                  try
                                                  {
                                                      JSONArray jsonArrayData = jsonObject.getJSONArray("data");
                                                      JSONObject dataObject;
                                                      for (int j = 0; j < jsonArrayData.length(); j++)
                                                      {

                                                          dataObject = (JSONObject) jsonArrayData.get(j);
                                                          friendsIdList.add(dataObject.getString("id"));
                                                      }

                                                      JSONObject jsonObjectSummary = jsonObject.getJSONObject("summary");
                                                      totalFriends = Integer
                                                              .parseInt(jsonObjectSummary.getString("total_count"));
                                                  }
                                                  catch (Exception e)
                                                  {
                                                      subscriber.onNext(null);
                                                      subscriber.onCompleted();
                                                      totalFriends = -1;
                                                      return;
                                                  }

                                                  int count = (totalFriends / 25) + 2;

                                                  for (int i = 0; i < count; i++)
                                                  {
                                                      GraphRequest nextPageRequest = response
                                                              .getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                                                      if (nextPageRequest != null)
                                                      {
                                                          nextPageRequest.setCallback(new GraphRequest.Callback()
                                                          {
                                                              @Override
                                                              public void onCompleted(GraphResponse graphResponse)
                                                              {
                                                                  response = graphResponse;
                                                                  JSONObject responseObject = response.getJSONObject();

                                                                  try
                                                                  {
                                                                      JSONArray jsonArrayData = responseObject
                                                                              .getJSONArray("data");
                                                                      JSONObject dataObject;
                                                                      for (int j = 0; j < jsonArrayData.length(); j++)
                                                                      {

                                                                          dataObject = (JSONObject) jsonArrayData.get(j);
                                                                          friendsIdList.add(dataObject.getString("id"));
                                                                      }
                                                                  }
                                                                  catch (Exception e)
                                                                  {
                                                                      subscriber.onNext(null);
                                                                      subscriber.onCompleted();
                                                                      totalFriends = -1;
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

                                  if (totalFriends != -1)
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

