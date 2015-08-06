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
import reaper.android.app.config.AppConstants;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.config.ErrorCode;
import reaper.android.app.trigger.common.GenericErrorTrigger;
import reaper.android.app.trigger.user.AllFacebookFriendsFetchedTrigger;
import reaper.android.app.trigger.user.FacebookFriendsFetchedTrigger;
import reaper.android.app.trigger.user.PhoneAddedTrigger;
import reaper.android.app.trigger.user.PhoneContactsFetchedTrigger;
import reaper.android.app.ui.util.PhoneUtils;
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

    public String getActiveUserId()
    {
        return "9320369679";
    }

    public String getActiveUserName()
    {
        return "Lucas Johnson";
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
                bus.post(new PhoneAddedTrigger());
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.PHONE_ADD_FAILURE, error));
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
                bus.post(new GenericErrorTrigger(ErrorCode.FACEBOOK_FRIENDS_FETCH_FAILURE, error));
            }
        });
    }

    public void getAllFacebookFriends()
    {
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        meApi.getAllFacebookFriends(new GetAllFacebookFriendsApiRequest(), new Callback<GetAllFacebookFriendsApiResponse>()
        {
            @Override
            public void success(GetAllFacebookFriendsApiResponse getAllFacebookFriendsApiResponse, Response response)
            {
                bus.post(new AllFacebookFriendsFetchedTrigger(getAllFacebookFriendsApiResponse.getFriends()));
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.ALL_FACEBOOK_FRIENDS_FETCH_FAILURE, error));
            }
        });
    }

    public void getPhoneContacts(ContentResolver contentResolver, String zone)
    {
        Set<String> contacts = fetchAllContacts(contentResolver);

        meApi = ApiManager.getInstance().getApi(MeApi.class);

        GetPhoneContactsApiRequest request = new GetPhoneContactsApiRequest(contacts, zone);
        meApi.getPhoneContacts(request, new Callback<GetPhoneContactsApiResponse>()
        {
            @Override
            public void success(GetPhoneContactsApiResponse getPhoneContactsApiResponse, Response response)
            {
                bus.post(new PhoneContactsFetchedTrigger(getPhoneContactsApiResponse.getPhoneContacts()));
            }

            @Override
            public void failure(RetrofitError error)
            {
                bus.post(new GenericErrorTrigger(ErrorCode.PHONE_CONTACTS_FETCH_FAILURE, error));
            }
        });
    }


    private Set<String> fetchAllContacts(ContentResolver contentResolver)
    {
        Set<String> allContacts = new HashSet<>();

        String defaultCountryCode = AppConstants.DEFAULT_COUNTRY_CODE;

        String[] PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);

        if (cur.moveToFirst())
        {
            do
            {
                // names comes in hand sometimes
                int id = Integer.parseInt(cur.getString(0));
                String phone = PhoneUtils.sanitize(cur.getString(1), defaultCountryCode);

                allContacts.add(phone);

            } while (cur.moveToNext());
        }

        cur.close();
        return allContacts;
    }

    public void sendBlockRequests(List<String> blockList, List<String> unblockList)
    {
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        meApi.blockFriends(new BlockFriendsApiRequest(blockList, unblockList), new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {

            }

            @Override
            public void failure(RetrofitError error)
            {

            }
        });
    }

    public void shareFeedback(String rating, String comment)
    {
        meApi = ApiManager.getInstance().getApi(MeApi.class);
        meApi.shareFeedback(new ShareFeedbackApiRequest(comment, rating), new Callback<Response>()
        {
            @Override
            public void success(Response response, Response response2)
            {

            }

            @Override
            public void failure(RetrofitError error)
            {

            }
        });
    }
}
