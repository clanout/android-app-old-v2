package reaper.android.app.ui.screens.launch.mvp.bootstrap;

import java.util.List;

import reaper.android.app.model.Event;

public interface BootstrapView
{
    void handleLocationPermissions();

    void displayLocationServiceUnavailableMessage();

    void displayError();

    void proceed(List<Event> events);
}
