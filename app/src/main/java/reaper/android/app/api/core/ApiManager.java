package reaper.android.app.api.core;

import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
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
    private static Gson gson;

    static
    {
        gson = (Converters.registerAll(new GsonBuilder())).registerTypeAdapter(DateTime.class, new DateTimeParser()).create();

        restAdapter = new RestAdapter.Builder().setClient(new OkClient(new OkHttpClient())).setConverter(new
                GsonConverter(gson)).setEndpoint(AppConstants.SERVER_URL).build();
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
        Log.d("reap3r", clazz.getName());
        T api = (T) apiMap.get(clazz);
        if (api == null)
        {
            api = restAdapter.create(clazz);
            apiMap.put(clazz, api);
        }

        return api;
    }

    private static class DateTimeParser implements JsonDeserializer<DateTime>
    {
        @Override
        public DateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext
                jsonDeserializationContext) throws JsonParseException
        {
            return DateTime.parse(jsonElement.getAsString());
        }
    }
}
