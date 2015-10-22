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
        eventSuggestionList.add(new CreateEventModel(EventCategory.CAFE, "Coffee :)"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.SHOPPING, "Who shops alone?"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.INDOORS, "The call of duty"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.CAFE, "Escape the office. Get a coffee break!"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.SPORTS, "Are you game?"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.DRINKS, "Lose Control"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.CAFE, "Catch up on Coffee"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.DRINKS, "Party till 4!"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.CAFE, "I <3 coffee"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.DRINKS, "A beer everyday makes me feel yay!"));
        eventSuggestionList.add(new CreateEventModel(EventCategory.CAFE, "Coffee :)"));

        return eventSuggestionList;
    }
}
