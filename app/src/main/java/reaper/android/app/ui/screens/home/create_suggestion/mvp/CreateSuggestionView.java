package reaper.android.app.ui.screens.home.create_suggestion.mvp;

import java.util.List;

import reaper.android.app.model.CreateEventSuggestion;
import reaper.android.app.model.EventCategory;

public interface CreateSuggestionView
{
    void init(List<CreateEventSuggestion> suggestions);

    void navigateToCreate(EventCategory category);
}
