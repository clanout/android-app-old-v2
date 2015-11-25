package reaper.android.app.ui.screens.details;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import reaper.android.app.config.BundleKeys;
import reaper.android.app.model.Event;

public class EventDetailsPagerAdapter extends FragmentStatePagerAdapter
{
    private List<Event> events;
    private int activePosition;
    private boolean shouldPopupStatusDialog;

    public EventDetailsPagerAdapter(FragmentManager fm, List<Event> events, int activePosition, boolean shouldPopupStatusDialog)
    {
        super(fm);
        this.events = events;
        this.shouldPopupStatusDialog = shouldPopupStatusDialog;
        this.activePosition = activePosition;
    }

    @Override
    public Fragment getItem(int position)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_FRAGMENT_EVENT, events.get(position));

//        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
//
//        if(position == activePosition)
//        {
//            bundle.putBoolean(BundleKeys.POPUP_STATUS_DIALOG, shouldPopupStatusDialog);
//        }else{
//            bundle.putBoolean(BundleKeys.POPUP_STATUS_DIALOG, false);
//        }
//
//        eventDetailsFragment.setArguments(bundle);
//        return eventDetailsFragment;

        return reaper.android.app.ui.screens.details.redesign.EventDetailsFragment
                .newInstance(events.get(position));
    }

    @Override
    public int getCount()
    {
        return events.size();
    }
}
