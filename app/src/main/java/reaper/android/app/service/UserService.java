package reaper.android.app.service;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.me.MeApi;
import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.api.me.request.BlockFriendsApiRequest;
import reaper.android.app.api.me.request.GetAllFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.me.request.GetPhoneContactsApiRequest;
import reaper.android.app.api.me.request.ShareFeedbackApiRequest;
import reaper.android.app.api.me.response.GetAllFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetFacebookFriendsApiResponse;
import reaper.android.app.api.me.response.GetPhoneContactsApiResponse;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.model.Friend;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllFacebookFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsFetchedTrigger;
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

    public void getFacebookFriends(final String zone)
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
                                    GetFacebookFriendsApiRequest request = new GetFacebookFriendsApiRequest(zone);
                                    return meApi.getFacebookFriends(request)
                                            .map(new Func1<GetFacebookFriendsApiResponse, List<Friend>>()
                                            {
                                                @Override
                                                public List<Friend> call(GetFacebookFriendsApiResponse getFacebookFriendsApiResponse)
                                                {
                                                    return getFacebookFriendsApiResponse
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
                        bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_FRIENDS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> friends)
                    {
                        bus.post(new FacebookFriendsFetchedTrigger(friends));
                    }
                });
    }

    public void getAllFacebookFriends()
    {
        GetAllFacebookFriendsApiRequest request = new GetAllFacebookFriendsApiRequest();

        meApi.getAllFacebookFriends(request)
                .map(new Func1<GetAllFacebookFriendsApiResponse, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(GetAllFacebookFriendsApiResponse getAllFacebookFriendsApiResponse)
                    {
                        return getAllFacebookFriendsApiResponse.getFriends();
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
                        bus.post(new GenericErrorTrigger(ErrorCode.ALL_FACEBOOK_FRIENDS_FETCH_FAILURE, (Exception) e));
                    }

                    @Override
                    public void onNext(List<Friend> friends)
                    {
                        bus.post(new AllFacebookFriendsFetchedTrigger(friends));
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
                        Log.d("APP", e.getMessage());
                    }

                    @Override
                    public void onNext(Response response)
                    {

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
                        Log.d("APP", e.getMessage());
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
