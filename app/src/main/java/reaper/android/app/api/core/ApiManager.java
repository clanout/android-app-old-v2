package reaper.android.app.api.core;

import com.squareup.okhttp.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

import reaper.android.app.config.AppConstants;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class ApiManager
{
    private static ApiManager instance;

    private static RestAdapter restAdapter;

    static
    {
        restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(new OkHttpClient()))
                .setConverter(new GsonConverter(GsonProvider.getGson()))
                .setEndpoint(AppConstants.SERVER_URL)
                .build();
    }

    private Map<Class<?>, Object> apiMap;

    private ApiManager()
    {
        apiMap = new HashMap<>();
    }

    public static ApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiManager();
        }

        return instance;
    }

    public <T> T getApi(Class<T> clazz)
    {
        T api = (T) apiMap.get(clazz);
        if (api == null)
        {
            api = restAdapter.create(clazz);
            apiMap.put(clazz, api);
        }

        return api;
    }
}
