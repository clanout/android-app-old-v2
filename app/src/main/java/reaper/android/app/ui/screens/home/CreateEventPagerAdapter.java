package reaper.android.app.ui.screens.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import reaper.android.app.model.CreateEventModel;

public class CreateEventPagerAdapter extends FragmentStatePagerAdapter
{
    List<CreateEventModel> models;

    public CreateEventPagerAdapter(FragmentManager fm, List<CreateEventModel> models)
    {
        super(fm);
        this.models = models;
    }

    @Override
    public Fragment getItem(int position)
    {
        return CreateEventFragment.newInstance(models.get(position));
    }

    @Override
    public int getCount()
    {
        return models.size();
    }
}
