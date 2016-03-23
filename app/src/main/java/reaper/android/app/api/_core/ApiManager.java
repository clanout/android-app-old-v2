package reaper.android.app.api._core;

import com.facebook.AccessToken;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import reaper.android.app.api.auth.AuthApi;
import reaper.android.app.api.event.EventApi;
import reaper.android.app.api.fb.FacebookApi;
import reaper.android.app.api.google_places.GooglePlacesApi;
import reaper.android.app.api.notification.NotificationApi;
import reaper.android.app.api.user.UserApi;
import reaper.android.app.config.AppConstants;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class ApiManager
{
    private static ApiManager instance;

    private static ApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiManager();
        }

        return instance;
    }

    private RestAdapter defaultRestAdapter;
    private RestAdapter facebookRestAdapter;
    private RestAdapter googlePlacesRestAdapter;

    private AuthApi authApi;
    private UserApi userApi;
    private EventApi eventApi;
    private NotificationApi notificationApi;
    private GooglePlacesApi googlePlacesApi;
    private FacebookApi facebookApi;

    private ApiManager()
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(15, TimeUnit.SECONDS);

        defaultRestAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint(AppConstants.BASE_URL_SERVER)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        googlePlacesRestAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(new RequestInterceptor()
                {
                    @Override
                    public void intercept(RequestFacade request)
                    {
                        request.addQueryParam("key", AppConstants.GOOGLE_API_KEY);
                    }
                })
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint(AppConstants.BASE_URL_GOOGLE_PLACES_API)
                .build();

        facebookRestAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint(AppConstants.BASE_URL_FACEBOOK_API)
                .setRequestInterceptor(new RequestInterceptor()
                {
                    @Override
                    public void intercept(RequestFacade request)
                    {
                        request.addQueryParam("access_token", AccessToken.getCurrentAccessToken()
                                                                         .getToken());
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    public static AuthApi getAuthApi()
    {
        if (getInstance().authApi == null)
        {
            getInstance().authApi = getInstance().defaultRestAdapter.create(AuthApi.class);
        }

        return getInstance().authApi;
    }

    public static UserApi getUserApi()
    {
        if (getInstance().userApi == null)
        {
            getInstance().userApi = getInstance().defaultRestAdapter.create(UserApi.class);
        }

        return getInstance().userApi;
    }

    public static EventApi getEventApi()
    {
        if (getInstance().eventApi == null)
        {
            getInstance().eventApi = getInstance().defaultRestAdapter.create(EventApi.class);
        }

        return getInstance().eventApi;
    }

    public static NotificationApi getNotificationApi()
    {
        if (getInstance().notificationApi == null)
        {
            return getInstance().defaultRestAdapter.create(NotificationApi.class);
        }

        return getInstance().notificationApi;
    }

    public static GooglePlacesApi getGooglePlacesApi()
    {
        if (getInstance().googlePlacesApi == null)
        {
            getInstance().googlePlacesApi = getInstance().googlePlacesRestAdapter
                    .create(GooglePlacesApi.class);
        }

        return getInstance().googlePlacesApi;
    }

    public static FacebookApi getFacebookApi()
    {
        if (getInstance().facebookApi == null)
        {
            getInstance().facebookApi = getInstance().facebookRestAdapter.create(FacebookApi.class);
        }

        return getInstance().facebookApi;
    }
}
