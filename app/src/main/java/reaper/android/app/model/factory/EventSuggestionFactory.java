package reaper.android.app.model.factory;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.EventSuggestion;

/**
 * Created by harsh on 21/09/15.
 */
public class EventSuggestionFactory {

    public static List<EventSuggestion> getEventSuggestions() {

        List<EventSuggestion> eventSuggestionList = new ArrayList<>();

        EventSuggestion eventSuggestion1 = new EventSuggestion();
        eventSuggestion1.setTitle("Sing out at a Karioke");
        eventSuggestion1.setCategory("Party");
        eventSuggestion1.setSuggestedDateTime(DateTime.now().plusHours(3));

        EventSuggestion eventSuggestion2 = new EventSuggestion();
        eventSuggestion2.setTitle("Want to have a beer?");
        eventSuggestion2.setCategory("Drinks");
        eventSuggestion2.setSuggestedDateTime(DateTime.now().plusHours(5));

        EventSuggestion eventSuggestion3 = new EventSuggestion();
        eventSuggestion3.setTitle("Play Football");
        eventSuggestion3.setCategory("Sports");
        eventSuggestion3.setSuggestedDateTime(DateTime.now().plusHours(2));

        EventSuggestion eventSuggestion4 = new EventSuggestion();
        eventSuggestion4.setTitle("Long drive away from the city?");
        eventSuggestion4.setCategory("Outdoors");
        eventSuggestion4.setSuggestedDateTime(DateTime.now().plusHours(9));

        eventSuggestionList.add(eventSuggestion1);
        eventSuggestionList.add(eventSuggestion2);
        eventSuggestionList.add(eventSuggestion3);
        eventSuggestionList.add(eventSuggestion4);

        return eventSuggestionList;
    }
}
