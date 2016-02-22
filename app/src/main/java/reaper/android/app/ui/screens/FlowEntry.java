package reaper.android.app.ui.screens;

import android.support.annotation.IntDef;

@IntDef({
        FlowEntry.HOME,
        FlowEntry.DETAILS,
        FlowEntry.DETAILS_WITH_STATUS_DIALOG,
        FlowEntry.NOTIFICATIONS,
        FlowEntry.CHAT
})
public @interface FlowEntry
{
    int HOME = 0;
    int DETAILS = 1;
    int DETAILS_WITH_STATUS_DIALOG = 2;
    int NOTIFICATIONS = 3;
    int CHAT = 4;
}
