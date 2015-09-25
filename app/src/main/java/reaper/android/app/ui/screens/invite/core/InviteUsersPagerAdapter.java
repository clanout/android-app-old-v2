package reaper.android.app.ui.screens.invite.core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui.screens.invite.facebook.InviteFacebookFriendsFragment;
import reaper.android.app.ui.screens.invite.phone.InvitePhoneContactsFragment;

public class InviteUsersPagerAdapter extends FragmentStatePagerAdapter
{
    private ArrayList<EventDetails.Invitee> inviteeList;
    private ArrayList<EventDetails.Attendee> attendeeList;
    private Event event;

    public InviteUsersPagerAdapter(FragmentManager fm, ArrayList<EventDetails.Invitee> inviteeList, ArrayList<EventDetails.Attendee> attendeeList, Event event)
    {
        super(fm);

        this.inviteeList = inviteeList;
        this.attendeeList = attendeeList;
        this.event = event;

        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITEE_LIST, inviteeList);
        bundle.putSerializable(BundleKeys.ATTENDEE_LIST, attendeeList);
    }

    @Override
    public Fragment getItem(int position)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.INVITEE_LIST, inviteeList);
        bundle.putSerializable(BundleKeys.ATTENDEE_LIST, attendeeList);
        bundle.putSerializable(BundleKeys.EVENT, event);

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
            // TODO -- Chnge Tab names

            case 0:
                return "Facebook Friends";
            case 1:
                return "Phone Contacts";
        }
        return null;
    }
}
