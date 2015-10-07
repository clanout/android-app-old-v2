package reaper.android.app.ui.screens.invite.core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;

import reaper.android.R;
import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.root.Reaper;
import reaper.android.app.ui.screens.invite.facebook.InviteFacebookFriendsFragment;
import reaper.android.app.ui.screens.invite.phone.InvitePhoneContactsFragment;
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
                InviteFacebookFriendsFragment fragment = new InviteFacebookFriendsFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                InvitePhoneContactsFragment contactsFragment = new InvitePhoneContactsFragment();
                contactsFragment.setArguments(bundle);
                return contactsFragment;
            case 2:
                InviteThroughSMSFragment inviteThroughSMSFragment = new InviteThroughSMSFragment();
                return inviteThroughSMSFragment;
        }
        return null;
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Facebook";
            case 1:
                return "On App";
            case 2:
                return "Phonebook";
        }
        return null;
    }
}
