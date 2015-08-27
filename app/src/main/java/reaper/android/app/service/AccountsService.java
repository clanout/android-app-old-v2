package reaper.android.app.service;

import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.ui.screens.accounts.AccountsListItem;

public class AccountsService
{
    public static List<AccountsListItem> getmenuList()
    {
        List<AccountsListItem> menuList = new ArrayList<>();
        int[] icons = {R.drawable.ic_action_group, R.drawable.ic_action_phone, R.drawable.ic_action_add_person, R.drawable.ic_action_settings, R.drawable.ic_action_secure};
        String[] titles = {"Manage Friends","Update Phone Number", "Invite People Through Whatsapp", "Share Feedback", "FAQs"};

        for (int i = 0; i < icons.length && i < titles.length; i++)
        {
            AccountsListItem current = new AccountsListItem();

            current.icon = icons[i];
            current.title = titles[i];

            menuList.add(current);
        }
        return menuList;
    }

    public static boolean appInstalledOrNot(String uri, PackageManager packageManager) {
        boolean app_installed = false;
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}
