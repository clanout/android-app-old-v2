package reaper.android.app.model.util;

import java.util.Comparator;

import reaper.android.app.model.NotificationWrapper;

public class NotificationComparator implements Comparator<NotificationWrapper>
{
    @Override
    public int compare(NotificationWrapper lhs, NotificationWrapper rhs)
    {
        if (lhs.getTimestamp().isAfter(rhs.getTimestamp()))
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
}
