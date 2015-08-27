package reaper.android.app.service;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.me.MeApi;
import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.BlockFriendsApiRequest;
import reaper.android.app.api.me.request.GetAllAppFriendsApiRequest;
import reaper.android.app.api.me.request.GetAppFriendsApiRequest;
import reaper.android.app.api.me.request.GetPhoneContactsApiRequest;
import reaper.android.app.api.me.request.ShareFeedbackApiRequest;
import reaper.android.app.api.me.request.UpdateFacebookFriendsApiRequest;
import reaper.android.app.api.me.response.GetAllAppFriendsApiResponse;
import reaper.android.app.api.me.response.GetAppFriendsApiResponse;
import reaper.android.app.api.me.response.GetPhoneContactsApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Friend;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllAppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedFromNetworkTrigger;
import reaper.android.app.trigger.user.AppFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsUpdatedOnServerTrigger;
import reaper.android.app.trigger.user.PhoneAddedTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.util.PhoneUtils;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UserService
{
    private static final String TAG = "UserService";

    private Bus bus;
    private MeApi meApi;
    private UserCache userCache;
    private GenericCache cache;

    public UserService(Bus bus)
    {
        this.bus = bus;
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        userCache = CacheManager.getUserCache();
        cache = CacheManager.getGenericCache();
    }

    public String getActiveUserId()
    {
        String userId = cache.get(CacheKeys.USER_ID);
        return userId;
    }

    public String getActiveUserName()
    {
        String name = cache.get(CacheKeys.USER_NAME);
        return name;
    }

    public void updatePhoneNumber(final String phoneNumber)
    {
        AddPhoneApiRequest request = new AddPhoneApiRequest(phoneNumber);

        meApi.updatePhoneNumber(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.PHONE_ADD_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if (response.getStatus() == 200)
                        {
                            cache.put(CacheKeys.MY_PHONE_NUMBER, phoneNumber);
                            bus.post(new PhoneAddedTrigger());
                        } else
                        {
                            bus.post(new GenericErrorTrigger(ErrorCode.PHONE_ADD_FAILURE, null));
                        }
                    }
                });
    }

    public void getAppFriendsFromNetwork(final String zone)
    {
        meApi.getAppFriends(new GetAppFriendsApiRequest(zone))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GetAppFriendsApiResponse>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.APP_FRIENDS_FETCH_FROM_NETWORK_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(GetAppFriendsApiResponse getAppFriendsApiResponse)
                    {
                        bus.post(new AppFriendsFetchedFromNetworkTrigger(getAppFriendsApiResponse.getFriends()));
                        userCache.deleteFriends();
                        userCache.saveFriends(getAppFriendsApiResponse.getFriends());
                    }
                });
    }

    public void getAppFriends(final String zone)
    {
        Observable<List<Friend>> friendsObservable =
                userCache.getFriends()
                        .flatMap(new Func1<List<Friend>, Observable<List<Friend>>>()
                        {
                            @Override
                            public Observable<List<Friend>> call(List<Friend> friends)
                            {
                                if (friends.isEmpty())
                                {
                                    GetAppFriendsApiRequest request = new GetAppFriendsApiRequest(zone);
                                    return meApi.getAppFriends(request)
                                            .map(new Func1<GetAppFriendsApiResponse, List<Friend>>()
                                            {
                                                @Override
                                                public List<Friend> call(GetAppFriendsApiResponse getAppFriendsApiResponse)
                                                {
                                                    return getAppFriendsApiResponse
                                                            .getFriends();
                                                }
                                            })
                                            .doOnNext(new Action1<List<Friend>>()
                                            {
                                                @Override
                                                public void call(List<Friend> friends)
                                                {
                                                    userCache.saveFriends(friends);
                                                }
                                            })
                                            .subscribeOn(Schedulers.newThread());
                                } else
                                {
                                    return Observable.just(friends);
                                }
                            }
                        });

        friendsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Friend>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.USER_APP_FRIENDS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> friends)
                    {
                        bus.post(new AppFriendsFetchedTrigger(friends));
                    }
                });
    }

    public void getAllAppFriends()
    {
        GetAllAppFriendsApiRequest request = new GetAllAppFriendsApiRequest();

        meApi.getAllAppFriends(request)
                .map(new Func1<GetAllAppFriendsApiResponse, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(GetAllAppFriendsApiResponse getAllAppFriendsApiResponse)
                    {
                        return getAllAppFriendsApiResponse.getFriends();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Friend>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.USER_ALL_APP_FRIENDS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> friends)
                    {
                        bus.post(new AllAppFriendsFetchedTrigger(friends));
                    }
                });
    }

    public void getPhoneContacts()
    {
        userCache.getContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Friend>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.PHONE_CONTACTS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> friends)
                    {
                        bus.post(new PhoneContactsFetchedTrigger(friends));
                    }
                });
    }

    public void refreshPhoneContacts(ContentResolver contentResolver, String zone)
    {
        Set<String> allContacts = fetchAllContacts(contentResolver);
        GetPhoneContactsApiRequest request = new GetPhoneContactsApiRequest(allContacts, zone);

        meApi.getPhoneContacts(request)
                .map(new Func1<GetPhoneContactsApiResponse, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(GetPhoneContactsApiResponse getPhoneContactsApiResponse)
                    {
                        return getPhoneContactsApiResponse.getPhoneContacts();
                    }
                })
                .doOnNext(new Action1<List<Friend>>()
                {
                    @Override
                    public void call(List<Friend> contacts)
                    {
                        userCache.saveContacts(contacts);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Friend>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.PHONE_CONTACTS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> contacts)
                    {
                        bus.post(new PhoneContactsFetchedTrigger(contacts));
                    }
                });
    }

    public void sendBlockRequests(List<String> blockList, List<String> unblockList)
    {
        BlockFriendsApiRequest request = new BlockFriendsApiRequest(blockList, unblockList);

        meApi.blockFriends(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {

                    }
                });
    }

    public void updateFacebookFriends(List<String> friendIdList, final boolean isPolling)
    {
        UpdateFacebookFriendsApiRequest request = new UpdateFacebookFriendsApiRequest(friendIdList);

        meApi.updateFacebookFriends(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_FRIENDS_UPDATION_ON_SERVER_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(Response response)
                    {
                        if(response.getStatus() == 200)
                        {
                            bus.post(new FacebookFriendsUpdatedOnServerTrigger(isPolling));
                        }
                    }
                });
    }

    public void shareFeedback(String rating, String comment)
    {
        ShareFeedbackApiRequest request = new ShareFeedbackApiRequest(comment, rating);

        meApi.shareFeedback(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {

                    }
                });
    }

    private Set<String> fetchAllContacts(ContentResolver contentResolver)
    {
        Set<String> allContacts = new HashSet<>();

        String defaultCountryCode = AppConstants.DEFAULT_COUNTRY_CODE;

        String[] PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cur = contentResolver
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);

        if (cur.moveToFirst())
        {
            do
            {
                String phone = PhoneUtils.sanitize(cur.getString(0), defaultCountryCode);
                allContacts.add(phone);
            } while (cur.moveToNext());
        }

        cur.close();
        return allContacts;
    }
}
