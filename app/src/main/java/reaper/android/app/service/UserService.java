package reaper.android.app.service;

import android.util.Pair;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reaper.android.app.api._core.ApiManager;
import reaper.android.app.api._core.GsonProvider;
import reaper.android.app.api.user.UserApi;
import reaper.android.app.api.user.request.BlockFriendsApiRequest;
import reaper.android.app.api.user.request.GetFacebookFriendsApiRequest;
import reaper.android.app.api.user.request.GetRegisteredContactsApiRequest;
import reaper.android.app.api.user.request.ShareFeedbackApiRequest;
import reaper.android.app.api.user.request.UpdateMobileAPiRequest;
import reaper.android.app.api.user.response.GetFacebookFriendsApiResponse;
import reaper.android.app.api.user.response.GetRegisteredContactsApiResponse;
import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.cache.user.UserCache;
import reaper.android.app.config.GenericCacheKeys;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Friend;
import reaper.android.app.model.User;
import reaper.android.app.model.util.FriendsComparator;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.service._new.PhonebookService_;
import reaper.android.common.analytics.AnalyticsHelper;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class UserService
{
    private static UserService instance;

    public static void init(LocationService_ locationService, PhonebookService_ phonebookService)
    {
        instance = new UserService(locationService, phonebookService);
    }

    public static UserService getInstance()
    {
        if (instance == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z11, false);
            /* Analytics */

            throw new IllegalStateException("[UserService Not Initialized]");
        }

        return instance;
    }

    private UserApi userApi;
    private UserCache userCache;

    private GenericCache genericCache;
    private LocationService_ locationService;
    private PhonebookService_ phonebookService;

    private User activeUser;

    private UserService(LocationService_ locationService, PhonebookService_ phonebookService)
    {
        this.locationService = locationService;
        this.phonebookService = phonebookService;

        userApi = ApiManager.getUserApi();
        userCache = CacheManager.getUserCache();
        genericCache = CacheManager.getGenericCache();
    }

    /* Session User */
    public void setSessionUser(User user)
    {
        if (user == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z11, false);
            /* Analytics */

            throw new IllegalStateException("[Session User null]");
        }

        activeUser = user;
        genericCache.put(GenericCacheKeys.SESSION_USER, user);
    }

    public User getSessionUser()
    {
        if (activeUser == null) {
            activeUser = genericCache.get(GenericCacheKeys.SESSION_USER, User.class);
        }

        return activeUser;
    }

    public String getSessionId()
    {
        if (getSessionUser() == null) {
            return null;
        }

        return getSessionUser().getSessionId();
    }

    public String getSessionUserId()
    {
        if (getSessionUser() == null) {
            return null;
        }

        return getSessionUser().getId();
    }

    public String getSessionUserName()
    {
        if (getSessionUser() == null) {
            return null;
        }

        return getSessionUser().getName();
    }

    /* Update Mobile Number */
    public void updatePhoneNumber(final String phoneNumber)
    {
        UpdateMobileAPiRequest request = new UpdateMobileAPiRequest(phoneNumber);

        userApi.updateMobile(request)
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
                        activeUser.setMobileNumber(phoneNumber);
                        setSessionUser(activeUser);
                    }
                });
    }

    /* Block/Unblock Facebook Friends */
    public void sendBlockRequests(List<String> blockList, List<String> unblockList)
    {
        BlockFriendsApiRequest request = new BlockFriendsApiRequest(blockList, unblockList);
        userApi.blockFriends(request)
                .subscribeOn(Schedulers.newThread())
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
                        CacheManager.clearFriendsCache();
                    }
                });
    }

    /* Facebook Friends */
    public Observable<List<Friend>> _fetchLocalFacebookFriends()
    {
        return _fetchLocalFacebookFriendsCache()
                .flatMap(new Func1<List<Friend>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<Friend> cachedFriends)
                    {
                        if (!cachedFriends.isEmpty()) {
                            return Observable.just(cachedFriends);
                        }
                        else {
                            return _fetchFacebookFriendsNetwork(false)
                                    .map(new Func1<Pair<List<Friend>, List<Friend>>, List<Friend>>()
                                    {
                                        @Override
                                        public List<Friend> call(Pair<List<Friend>, List<Friend>>
                                                                         allFriends)
                                        {
                                            return allFriends.first;
                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Pair<List<Friend>, List<Friend>>> _fetchFacebookFriendsNetwork(final
                                                                                     boolean fetchAll)
    {
        GetFacebookFriendsApiRequest request = new GetFacebookFriendsApiRequest(null);
        if (!fetchAll) {
            String zone = locationService.getCurrentLocation().getZone();
            request = new GetFacebookFriendsApiRequest(zone);
        }

        return userApi.getFacebookFriends(request)
                .map(new Func1<GetFacebookFriendsApiResponse, Pair<List<Friend>,
                        List<Friend>>>()
                {
                    @Override
                    public Pair<List<Friend>, List<Friend>> call
                            (GetFacebookFriendsApiResponse response)
                    {
                        List<Friend> localFriends = response.getFriends();
                        List<Friend> otherFriends = response.getOtherFriends();

                        identifyNewFriends(localFriends, otherFriends);

                        Collections.sort(localFriends, new FriendsComparator());
                        Collections.sort(otherFriends, new FriendsComparator());
                        return new Pair<>(localFriends, otherFriends);
                    }
                })
                .doOnNext(new Action1<Pair<List<Friend>, List<Friend>>>()
                {
                    @Override
                    public void call(Pair<List<Friend>, List<Friend>> friends)
                    {
                        List<Friend> localFriends = friends.first;
                        // Cache Local Friends
                        userCache.saveFriends(localFriends);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private void identifyNewFriends(List<Friend> localFriends, List<Friend> otherFriends)
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> newFriends = GsonProvider.getGson().fromJson(CacheManager.getGenericCache
                ().get
                (GenericCacheKeys.NEW_FRIENDS_LIST), type);

        if (newFriends != null) {
            for (Friend friend : localFriends) {
                if (newFriends.contains(friend)) {
                    friend.setIsNew(true);
                }
                else {
                    friend.setIsNew(false);
                }
            }

            for (Friend friend : otherFriends) {
                if (newFriends.contains(friend)) {
                    friend.setIsNew(true);
                }
                else {
                    friend.setIsNew(false);
                }
            }
        }
        else {

            for (Friend friend : localFriends) {
                friend.setIsNew(false);
            }

            for (Friend friend : otherFriends) {
                friend.setIsNew(false);
            }
        }
    }

    public Observable<List<Friend>> _fetchLocalFacebookFriendsCache()
    {
        return userCache
                .getFriends()
                .subscribeOn(Schedulers.newThread());
    }

    /* Registered Phonebook Contacts */
    public Observable<List<Friend>> _fetchLocalRegisteredContacts()
    {
        return _fetchLocalRegisteredContactsCache()
                .flatMap(new Func1<List<Friend>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<Friend> cachedContacts)
                    {
                        if (!cachedContacts.isEmpty()) {
                            return Observable.just(cachedContacts);
                        }
                        else {
                            return _fetchRegisteredContactsNetwork(false);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _fetchRegisteredContactsNetwork(final boolean fetchAll)
    {
        return phonebookService
                .fetchAllNumbers()
                .flatMap(new Func1<List<String>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<String> allContacts)
                    {
                        GetRegisteredContactsApiRequest request = new
                                GetRegisteredContactsApiRequest(allContacts, null);
                        if (!fetchAll) {
                            String zone = locationService.getCurrentLocation().getZone();
                            request = new GetRegisteredContactsApiRequest(allContacts, zone);
                        }

                        return userApi
                                .getRegisteredContacts(request)
                                .map(new Func1<GetRegisteredContactsApiResponse, List<Friend>>()
                                {
                                    @Override
                                    public List<Friend> call(GetRegisteredContactsApiResponse
                                                                     response)
                                    {
                                        List<Friend> registeredContacts = response
                                                .getRegisteredContacts();
                                        Collections
                                                .sort(registeredContacts, new FriendsComparator());
                                        return registeredContacts;
                                    }
                                })
                                .map(new Func1<List<Friend>, List<Friend>>()
                                {
                                    @Override
                                    public List<Friend> call(List<Friend> friends)
                                    {
                                        String sessionUserId = getSessionUserId();
                                        Friend me = new Friend();
                                        me.setId(sessionUserId);
                                        friends.remove(me);
                                        return friends;
                                    }
                                })
                                .doOnNext(new Action1<List<Friend>>()
                                {
                                    @Override
                                    public void call(List<Friend> contacts)
                                    {
                                        if (!fetchAll) {
                                            // Cache local registered contacts
                                            userCache.saveContacts(contacts);
                                        }
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _fetchLocalRegisteredContactsCache()
    {
        return userCache
                .getContacts()
                .subscribeOn(Schedulers.newThread());
    }

    /* App Friends */
    public Observable<List<Friend>> _fetchLocalAppFriends()
    {
        return Observable
                .zip(_fetchLocalFacebookFriends(), _fetchLocalRegisteredContacts(),
                        new Func2<List<Friend>, List<Friend>, List<Friend>>()
                        {
                            @Override
                            public List<Friend> call(List<Friend> facebookFriends, List<Friend>
                                    registeredContacts)
                            {
                                Set<Friend> allFriends = new HashSet<Friend>();
                                allFriends.addAll(facebookFriends);
                                allFriends.addAll(registeredContacts);
                                return new ArrayList<>(allFriends);
                            }
                        }
                )
                .map(new Func1<List<Friend>, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(List<Friend> friends)
                    {
                        Collections.sort(friends, new FriendsComparator());
                        return friends;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _refreshLocalAppFriends()
    {
        return Observable
                .zip(_fetchFacebookFriendsNetwork(false), _fetchRegisteredContactsNetwork(false),
                        new Func2<Pair<List<Friend>, List<Friend>>, List<Friend>, List<Friend>>()
                        {
                            @Override
                            public List<Friend> call(Pair<List<Friend>, List<Friend>>
                                                             allFacebookFriends, List<Friend>
                                                             registeredContacts)
                            {
                                List<Friend> localFacebookFriends = allFacebookFriends.first;

                                Set<Friend> allFriends = new HashSet<Friend>();
                                allFriends.addAll(localFacebookFriends);
                                allFriends.addAll(registeredContacts);
                                return new ArrayList<>(allFriends);
                            }
                        })
                .map(new Func1<List<Friend>, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(List<Friend> friends)
                    {
                        Collections.sort(friends, new FriendsComparator());
                        return friends;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Feedback */
    public void shareFeedback(int type, String comment)
    {
        ShareFeedbackApiRequest request = new ShareFeedbackApiRequest(comment, type);

        userApi.shareFeedback(request)
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

    /* New User */
    public void markUserAsOld()
    {
        activeUser.setNewUser(false);
        setSessionUser(activeUser);
    }
}
