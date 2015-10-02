package reaper.android.app.model.factory;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.EventCategory;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventSuggestionFactory {

    public static List<CreateEventModel> getEventSuggestions() {

        List<CreateEventModel> eventSuggestionList = new ArrayList<>();
        eventSuggestionList.add(new CreateEventModel(EventCategory.MOVIES, "Plan a movie tonight"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.EAT_OUT, "Hungry? Go eat out with friends"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.OUTDOORS, "Long drive away from the city"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.DRINKS, "Lose Control"));

        return eventSuggestionList;
    }
}
