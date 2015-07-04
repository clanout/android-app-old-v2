package reaper.android.app.service;

import com.squareup.otto.Bus;

import reaper.android.app.api.core.ApiManager;
import reaper.android.app.api.me.MeApi;
import reaper.android.app.api.me.request.AddPhoneApiRequest;
import reaper.android.app.config.CacheKeys;
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

}
