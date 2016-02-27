package reaper.android.app.ui.screens.invite;

import reaper.android.app.ui._core.PermissionHandler;

public interface InviteScreen
{
    void setReadContactsPermissionListener(PermissionHandler.Listener listener);

    void navigateToAppSettings();

    void navigateToDetailsScreen();
}
