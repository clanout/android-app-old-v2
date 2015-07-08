package reaper.android.app.trigger.event;

import java.util.List;

import reaper.android.app.model.Suggestion;

/**
 * Created by aditya on 04/07/15.
 */
public class EventSuggestionsTrigger
{
    private List<Suggestion> recommendations;

    public EventSuggestionsTrigger(List<Suggestion> recommendations)
    {
        this.recommendations = recommendations;
    }

    public List<Suggestion> getRecommendations()
    {
        return recommendations;
    }
}
