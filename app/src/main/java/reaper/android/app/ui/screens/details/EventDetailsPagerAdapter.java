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

    public EventDetailsPagerAdapter(FragmentManager fm, List<Event> events)
    {
        super(fm);
        this.events = events;
    }

    @Override
    public Fragment getItem(int position)
    {
        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKeys.EVENT_DETAILS_FRAGMENT_EVENT, events.get(position));
        eventDetailsFragment.setArguments(bundle);
        return eventDetailsFragment;
    }

    @Override
    public int getCount()
    {
        return events.size();
    }
}
