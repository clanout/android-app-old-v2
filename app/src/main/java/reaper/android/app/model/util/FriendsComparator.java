package reaper.android.app.model.util;

import java.util.Comparator;

import reaper.android.app.model.Friend;

/**
 * Created by Aditya on 27-08-2015.
 */
public class FriendsComparator implements Comparator<Friend>
{
    @Override
    public int compare(Friend lhs, Friend rhs)
    {
        if (lhs.isNew() && !rhs.isNew()) {
            return -1;
        }
        else if (!lhs.isNew() && rhs.isNew()) {
            return 1;
        }
        else {
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        }
    }
}
