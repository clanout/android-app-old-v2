package reaper.android.app.service._new;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import reaper.android.app.config.AppConstants;
import reaper.android.app.service.UserService;

public class WhatsappService_
{
    private static WhatsappService_ instance;

    public static void init(UserService userService)
    {
        instance = new WhatsappService_(userService);
    }

    public static WhatsappService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[AccountService Not Initialized]");
        }

        return instance;
    }

    private static final String WHATSAPP_MESSAGE = "%s wants to clan out with you. Join %s at %s";
    private static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";

    private UserService userService;

    private WhatsappService_(UserService userService)
    {
        this.userService = userService;
    }

    public boolean isWhatsAppInstalled(Activity activity)
    {
        try
        {
            activity.getPackageManager()
                    .getPackageInfo(WHATSAPP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    public Intent getWhatsAppIntent()
    {
        String message = String
                .format(WHATSAPP_MESSAGE, userService.getSessionUserName(), userService
                        .getSessionUserName(), AppConstants.APP_LINK);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        sendIntent.setPackage(WHATSAPP_PACKAGE_NAME);

        return sendIntent;
    }
}
