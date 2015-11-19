package reaper.android.app.model.factory;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.api.core.GsonProvider;
import reaper.android.app.cache.core.CacheManager;
import reaper.android.app.cache.generic.GenericCache;
import reaper.android.app.config.CacheKeys;
import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.EventCategory;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventSuggestionFactory {

    private List<CreateEventModel> eventSuggestions;

    public static List<CreateEventModel> getEventSuggestions() {

        GenericCache genericCache = CacheManager.getGenericCache();

        if(genericCache.get(CacheKeys.EVENT_SUGGESTIONS) == null)
        {
            Log.d("APP", "event suggestions not from cache -- static");

            List<CreateEventModel> eventSuggestions = new ArrayList<>();
            eventSuggestions.add(new CreateEventModel(EventCategory.CAFE, "Coffee :)"));
            eventSuggestions.add(new CreateEventModel(EventCategory.SHOPPING, "Who shops alone?"));
            eventSuggestions.add(new CreateEventModel(EventCategory.INDOORS, "The call of duty"));
            eventSuggestions.add(new CreateEventModel(EventCategory.CAFE, "Escape the office. Get a coffee break!"));
            eventSuggestions.add(new CreateEventModel(EventCategory.SPORTS, "Are you game?"));
            eventSuggestions.add(new CreateEventModel(EventCategory.DRINKS, "Lose Control"));
            eventSuggestions.add(new CreateEventModel(EventCategory.CAFE, "Catch up on Coffee"));
            eventSuggestions.add(new CreateEventModel(EventCategory.MOVIES, "Popcorn. Interval. Popcorn"));
            eventSuggestions.add(new CreateEventModel(EventCategory.DRINKS, "Party till 4!"));
            eventSuggestions.add(new CreateEventModel(EventCategory.CAFE, "I <3 coffee"));
            eventSuggestions.add(new CreateEventModel(EventCategory.INDOORS, "29"));
            eventSuggestions.add(new CreateEventModel(EventCategory.DRINKS, "A beer everyday makes me feel yay!"));
            eventSuggestions.add(new CreateEventModel(EventCategory.MOVIES, "Showtime!"));
            eventSuggestions.add(new CreateEventModel(EventCategory.EAT_OUT, "Share a happy meal"));
            eventSuggestions.add(new CreateEventModel(EventCategory.OUTDOORS, "Motorcycle Diaries"));
            eventSuggestions.add(new CreateEventModel(EventCategory.MOVIES, "Chicks don't miss flicks"));
            eventSuggestions.add(new CreateEventModel(EventCategory.DRINKS, "Happy is the hour!"));
            eventSuggestions.add(new CreateEventModel(EventCategory.INDOORS, "Go all in tonight"));
            eventSuggestions.add(new CreateEventModel(EventCategory.MOVIES, "Lights. Camera. Popcorn"));

            Collections.shuffle(eventSuggestions);

            eventSuggestions.add(eventSuggestions.get(0));

            return eventSuggestions;
        }else{

            Log.d("APP", "event suggestions from cache -- static");

            String suggestionsJson = genericCache.get(CacheKeys.EVENT_SUGGESTIONS);

            Gson gson = GsonProvider.getGson();

            Type type = new TypeToken<List<CreateEventModel>>(){

            }.getType();

            List<CreateEventModel> eventSuggestions = gson.fromJson(suggestionsJson, type);

            Collections.shuffle(eventSuggestions);
            eventSuggestions.add(eventSuggestions.get(0));
            return eventSuggestions;
        }
    }
}
