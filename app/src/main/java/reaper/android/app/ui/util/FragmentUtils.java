package reaper.android.app.ui.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import reaper.android.R;

public class FragmentUtils
{
    public static void changeFragment(FragmentManager manager, Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main, fragment, fragment.getTag());
        fragmentTransaction.commit();
    }
}
