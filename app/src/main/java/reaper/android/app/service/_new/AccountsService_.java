package reaper.android.app.service._new;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import reaper.android.app.config.AppConstants;
import reaper.android.app.service.UserService;

public class AccountsService_
{
    private static AccountsService_ instance;

    public static AccountsService_ getInstance()
    {
        if (instance == null)
        {
            instance = new AccountsService_();
        }

        return instance;
    }

    private AccountsService_()
    {
    }

    public boolean isWhatsAppInstalled(Activity activity)
    {
        try
        {
            activity.getPackageManager()
                    .getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    public Intent getWhatsAppIntent()
    {
        UserService userService = UserService.getInstance();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, userService.getSessionUserName() +
                AppConstants.WHATSAPP_INVITATION_MESSAGE + AppConstants.APP_LINK);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");

        return sendIntent;
    }
}
