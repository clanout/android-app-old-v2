package reaper.android.app.ui.screens.invite;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.EventDetails;

public class InviteUsersPagerAdapter extends FragmentPagerAdapter
{
    private InviteFacebookFriendsFragment inviteFacebookFriendsFragment;
    private InvitePhoneContactsFragment invitePhoneContactsFragment;
    private ArrayList<EventDetails.Invitee> inviteeList;

    public InviteUsersPagerAdapter(FragmentManager fm, ArrayList<EventDetails.Invitee> inviteeList)
    {
        super(fm);

        Bundle bundle = new Bundle();
        bundle.putSerializable("invitee_list", inviteeList);

        inviteFacebookFriendsFragment = new InviteFacebookFriendsFragment();
        invitePhoneContactsFragment = new InvitePhoneContactsFragment();

        inviteFacebookFriendsFragment.setArguments(bundle);
        invitePhoneContactsFragment.setArguments(bundle);

        this.inviteeList = inviteeList;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return inviteFacebookFriendsFragment;
            case 1:
                return invitePhoneContactsFragment;
        }
        return null;
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Facebook Friends";
            case 1:
                return "Phone Contacts";
        }
        return null;
    }
}
