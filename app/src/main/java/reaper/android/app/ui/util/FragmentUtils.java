package reaper.android.app.ui.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import reaper.android.R;

public class FragmentUtils
{
    public static void changeFragment(FragmentManager manager, Fragment fragment, boolean addToBackStack)
    {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main, fragment, fragment.getClass().getSimpleName());
        if (addToBackStack)
        {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public static void clearBackStack(FragmentManager manager)
    {
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
