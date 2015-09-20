package reaper.android.app.ui.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import reaper.android.R;

public class FragmentUtils
{
    public static void changeFragment(FragmentManager manager, Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
        manager.executePendingTransactions();
    }

    public static void clearBackStack(FragmentManager manager)
    {
        for (int i = 0; i < manager.getBackStackEntryCount(); ++i)
        {
            manager.popBackStack();
        }
    }
}
