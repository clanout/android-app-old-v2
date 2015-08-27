package reaper.android.app.model;

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
        return lhs.getName().compareTo(rhs.getName());
    }
}
