package reaper.android.app.ui.screens.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import reaper.android.app.model.CreateEventModel;

/**
 * Created by harsh on 21/09/15.
 */
public class CreateEventPagerAdapter extends FragmentStatePagerAdapter
{

    private List<CreateEventModel> eventSuggestionList;

    public CreateEventPagerAdapter(FragmentManager fm, List<CreateEventModel> eventSuggestionList)
    {
        super(fm);

        this.eventSuggestionList = eventSuggestionList;
    }

    @Override
    public Fragment getItem(int position)
    {
        return CreateEventFragment.newInstance(eventSuggestionList.get(position));
    }

    @Override
    public int getCount()
    {
        return eventSuggestionList.size();
    }
}
