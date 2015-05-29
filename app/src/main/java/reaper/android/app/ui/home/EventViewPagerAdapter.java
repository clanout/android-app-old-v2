package reaper.android.app.ui.home;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import reaper.android.R;

public class EventViewPagerAdapter extends PagerAdapter
{
    public Object instantiateItem(ViewGroup collection, int position)
    {
        int resId = 0;
        switch (position)
        {
            case 0:
                resId = R.id.cv_list_item_event;
                break;
            case 1:
                resId = R.id.secondaryContentFrameLayout;
                break;
        }
        return collection.findViewById(resId);
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == (View) object;
    }
}
