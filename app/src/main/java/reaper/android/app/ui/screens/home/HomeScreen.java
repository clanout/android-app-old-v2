package reaper.android.app.ui.screens.home;

import reaper.android.app.model.EventCategory;

public interface HomeScreen
{
    void navigateToCreateDetailsScreen(EventCategory category);

    void navigateToDetailsScreen(String eventId);
}
