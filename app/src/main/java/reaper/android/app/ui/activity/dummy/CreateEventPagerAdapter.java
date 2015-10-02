package reaper.android.app.ui.activity.dummy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.List;

import reaper.android.app.model.CreateEventModel;
import reaper.android.app.ui.screens.home.CreateEventFragment;

public class CreateEventPagerAdapter extends FragmentPagerAdapter
{
    List<CreateEventModel> models;

    public CreateEventPagerAdapter(FragmentManager fm, List<CreateEventModel> models, int size)
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
