package reaper.android.app.ui.screens.details.mvp;

import java.util.List;

import reaper.android.app.model.Event;

public interface EventDetailsContainerView
{
    void initView(List<Event> events, int activePosition);

    void handleError();
}
