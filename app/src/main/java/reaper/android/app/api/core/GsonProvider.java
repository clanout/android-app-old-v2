package reaper.android.app.api.core;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class GsonProvider
{
    private static Gson gson;

    static
    {
        gson = (Converters.registerAll(new GsonBuilder())).registerTypeAdapter(DateTime.class, new DateTimeParser()).create();
    }

    public static Gson getGson()
    {
        return gson;
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
