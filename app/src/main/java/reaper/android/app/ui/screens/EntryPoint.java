package reaper.android.app.ui.screens;

import android.support.annotation.IntDef;

@IntDef({EntryPoint.HOME, EntryPoint.DETAILS, EntryPoint.NOTIFICATIONS, EntryPoint.CHAT})
public @interface EntryPoint
{
    int HOME = 0;
    int DETAILS = 1;
    int NOTIFICATIONS = 2;
    int CHAT = 3;
}
