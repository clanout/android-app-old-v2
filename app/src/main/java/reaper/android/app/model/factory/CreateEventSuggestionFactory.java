package reaper.android.app.model.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.model.CreateEventModel;
import reaper.android.app.model.EventCategory;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventSuggestionFactory {

    private static List<CreateEventModel> eventSuggestions;

    static {

        eventSuggestions = new ArrayList<>();
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
    }

    public static List<CreateEventModel> getEventSuggestions() {

        List<CreateEventModel> shuffledEventSuggestions = new ArrayList<>();

        for (CreateEventModel createEventModel : eventSuggestions) {
            shuffledEventSuggestions.add(createEventModel);
        }

        Collections.shuffle(shuffledEventSuggestions);

        shuffledEventSuggestions.add(shuffledEventSuggestions.get(0));

        return shuffledEventSuggestions;
    }
}
