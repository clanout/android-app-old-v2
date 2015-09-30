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

        CreateEventModel model1 = new CreateEventModel(EventCategory.MOVIES, "Plan a movie tonight");
        CreateEventModel model2 = new CreateEventModel(EventCategory.EAT_OUT, "Hungry? Go eat out with friends");
        CreateEventModel model3 = new CreateEventModel(EventCategory.OUTDOORS, "Long drive away from the city");
        CreateEventModel model4 = new CreateEventModel(EventCategory.DRINKS, "Lose Control");


        eventSuggestionList.add(model1);
        eventSuggestionList.add(model2);
        eventSuggestionList.add(model3);
        eventSuggestionList.add(model4);

        return eventSuggestionList;
    }
}
