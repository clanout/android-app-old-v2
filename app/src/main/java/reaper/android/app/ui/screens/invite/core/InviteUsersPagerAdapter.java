package reaper.android.app.ui.screens.invite.core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.ui.screens.invite.app_friends.InviteAppFriendsFragment;
import reaper.android.app.ui.screens.invite.sms.InviteThroughSMSFragment;

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
                InviteAppFriendsFragment fragment = new InviteAppFriendsFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                InviteThroughSMSFragment inviteThroughSMSFragment = new InviteThroughSMSFragment();
                return inviteThroughSMSFragment;
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
                return "App Friends";
            case 1:
                return "Phonebook";
        }
        return null;
    }
}
