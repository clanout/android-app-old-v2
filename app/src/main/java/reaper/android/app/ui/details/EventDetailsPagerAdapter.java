package reaper.android.app.ui.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.List;

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
        bundle.putSerializable("event", events.get(position));
        eventDetailsFragment.setArguments(bundle);
        return eventDetailsFragment;
    }

    @Override
    public int getCount()
    {
        return events.size();
    }
}
