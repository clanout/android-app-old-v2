package reaper.android.app.ui.screens.invite.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui.screens.invite.facebook.InviteFacebookFriendsFragment;
import reaper.android.app.ui.screens.invite.phone.InvitePhoneContactsFragment;

public class InviteUsersPagerAdapter extends FragmentPagerAdapter
{
    private InviteFacebookFriendsFragment inviteFacebookFriendsFragment;
    private InvitePhoneContactsFragment invitePhoneContactsFragment;
    private ArrayList<EventDetails.Invitee> inviteeList;

    public InviteUsersPagerAdapter(FragmentManager fm, ArrayList<EventDetails.Invitee> inviteeList)
    {
        super(fm);

        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITEE_LIST, inviteeList);

        this.inviteeList = inviteeList;
    }

    @Override
    public Fragment getItem(int position)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITEE_LIST, inviteeList);

        switch (position)
        {
            case 0:
                InviteFacebookFriendsFragment fragment = new InviteFacebookFriendsFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                InvitePhoneContactsFragment contactsFragment = new InvitePhoneContactsFragment();
                contactsFragment.setArguments(bundle);
                return contactsFragment;
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
