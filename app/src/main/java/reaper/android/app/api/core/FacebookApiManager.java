package reaper.android.app.api.core;

import com.facebook.AccessToken;
import com.squareup.okhttp.OkHttpClient;

import reaper.android.app.api.fb.FacebookApi;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FacebookApiManager
{
    private static FacebookApiManager instance;

    private RestAdapter restAdapter;

    private FacebookApiManager()
    {
        // TODO: Shift endpoint to ca constant file
        // TODO : Remove logging
        restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint("https://graph.facebook.com/v2.4/")
                .setRequestInterceptor(new RequestInterceptor()
                {
                    @Override
                    public void intercept(RequestFacade request)
                    {
                        request.addQueryParam("access_token", AccessToken.getCurrentAccessToken().getToken());
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    public static FacebookApiManager getInstance()
    {
        if(instance == null)
        {
            instance = new FacebookApiManager();
        }
        return instance;
    }

    public static void reset()
    {
        instance = null;
    }

    public FacebookApi getApi()
    {
        return restAdapter.create(FacebookApi.class);
    }
}
