package reaper.android.app.ui.screens.home;

import org.joda.time.LocalTime;

import reaper.android.app.model.EventCategory;

public interface HomeScreen
{
    void navigateToCreateDetailsScreen(String title, EventCategory category, boolean isSecret, String startDay, LocalTime startTime);

    void navigateToDetailsScreen(String eventId);
}
