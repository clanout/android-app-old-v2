package reaper.android.app.service;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import reaper.android.R;
import reaper.android.app.root.Reaper;
import reaper.android.app.ui.screens.accounts.AccountsListItem;

public class AccountsService
{
    private static Drawable friendsDrawable, phoneDrawable, inviteWhatsappDrawable, shareFeedbackDrawable, faqDrawable;

    private static void generateDrawables()
    {
        if (friendsDrawable == null)
        {
            friendsDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.SNOWMAN)
                    .setColor(Color.CYAN)
                    .setSizeDp(24)
                    .build();
        }

        if (phoneDrawable == null)
        {
            phoneDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.PHONE)
                    .setColor(Color.CYAN)
                    .setSizeDp(24)
                    .build();
        }

        if (inviteWhatsappDrawable == null)
        {
            inviteWhatsappDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.WHATSAPP)
                    .setColor(Color.CYAN)
                    .setSizeDp(24)
                    .build();
        }

        if (shareFeedbackDrawable == null)
        {
            shareFeedbackDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.OWL)
                    .setColor(Color.CYAN)
                    .setSizeDp(24)
                    .build();
        }

        if (faqDrawable == null)
        {
            faqDrawable = MaterialDrawableBuilder.with(Reaper.getReaperContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.HELP)
                    .setColor(Color.CYAN)
                    .setSizeDp(24)
                    .build();
        }
    }

    public static List<AccountsListItem> getmenuList()
    {
        generateDrawables();
        List<AccountsListItem> menuList = new ArrayList<>();
        Drawable[] drawables = {friendsDrawable, phoneDrawable, inviteWhatsappDrawable, shareFeedbackDrawable, faqDrawable};
        String[] titles = {"Manage Friends", "Update Phone Number", "Invite People Through Whatsapp", "Share Feedback", "FAQs"};

        for (int i = 0; i < drawables.length && i < titles.length; i++)
        {
            AccountsListItem current = new AccountsListItem();

            current.drawable = drawables[i];
            current.title = titles[i];

            menuList.add(current);
        }
        return menuList;
    }

    public static boolean appInstalledOrNot(String uri, PackageManager packageManager)
    {
        boolean app_installed = false;
        try
        {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }
}
