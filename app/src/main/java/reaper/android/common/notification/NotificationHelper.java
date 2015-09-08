package reaper.android.common.notification;

import android.graphics.drawable.Drawable;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Map;

import reaper.android.R;
import reaper.android.app.root.Reaper;

public class NotificationHelper
{
    public static int getType(String name)
    {
        if (name.equals("EVENT_CREATED"))
        {
            return Notification.EVENT_CREATED;
        }
        else
        {
            throw new IllegalArgumentException("invalid event type [" + name + "]");
        }
    }

    public static String getMessage(int type, Map<String, String> args)
    {
        switch (type)
        {
            case Notification.EVENT_CREATED:
                return getEventCreatedMessage(args);
            default:
                return "";
        }
    }

    public static int getIcon(int type)
    {
        return R.drawable.ic_btn_rsvp_going;
    }

    private static String getEventCreatedMessage(Map<String, String> args)
    {
        return args.get("user_name") + " is starting a new clan";
    }
}
