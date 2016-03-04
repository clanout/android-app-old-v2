package reaper.android.app.ui.screens.home.create_suggestion.mvp;

import reaper.android.app.model.EventCategory;

public interface CreateSuggestionPresenter
{
    void attachView(CreateSuggestionView view);

    void detachView();

    void select(EventCategory category);
}
