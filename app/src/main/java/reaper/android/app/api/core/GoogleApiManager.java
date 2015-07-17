package reaper.android.app.api.core;

import com.squareup.okhttp.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

import reaper.android.app.api.google.GooglePlacesApi;
import reaper.android.app.config.AppConstants;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class GoogleApiManager
{
    private static GoogleApiManager instance;

    private static RequestInterceptor requestInterceptor;

    static
    {
        requestInterceptor = new RequestInterceptor()
        {
            @Override
            public void intercept(RequestFacade request)
            {
                request.addQueryParam("key", AppConstants.GOOGLE_API_KEY);
            }
        };
    }

    private Map<Class<?>, Object> apiMap;

    private GoogleApiManager()
    {
        apiMap = new HashMap<>();
    }

    public static GoogleApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new GoogleApiManager();
        }

        return instance;
    }

    public GooglePlacesApi getPlacesApi()
    {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint(AppConstants.GOOGLE_PLACES_API_URL)
                .build();

        return restAdapter.create(GooglePlacesApi.class);
    }
}
