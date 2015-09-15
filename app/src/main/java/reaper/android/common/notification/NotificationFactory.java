package reaper.android.common.notification;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.Map;

import reaper.android.app.api.core.GsonProvider;
import timber.log.Timber;

public class NotificationFactory
{
    private static final String TITLE = "clanOut";

    private static Type TYPE = new TypeToken<Map<String, String>>()
    {
    }.getType();

    public static Notification create(Bundle data)
    {
        try
        {
            String type = data.getString("type");
            Map<String, String> args = GsonProvider.getGson()
                    .fromJson(data.getString("parameters"), TYPE);

            int typeCode = NotificationHelper.getType(type);

            if ((typeCode == Notification.BLOCKED) || (typeCode == Notification.UNBLOCKED) || (typeCode == Notification.FRIEND_RELOCATED) || (typeCode == Notification.NEW_FRIEND_ADDED))
            {
                Notification notification =
                        new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                                .type(typeCode)
                                .title(TITLE)
                                .eventId("")
                                .timestamp(DateTime.now())
                                .message(NotificationHelper.getMessage(typeCode, args))
                                .isNew(true)
                                .args(args)
                                .build();
                return notification;
            } else
            {
                Notification notification =
                        new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                                .type(typeCode)
                                .title(TITLE)
                                .eventId(args.get("event_id"))
                                .timestamp(DateTime.now())
                                .message(NotificationHelper.getMessage(typeCode, args))
                                .isNew(true)
                                .args(args)
                                .build();
                return notification;
            }

        } catch (Exception e)
        {
            Timber.e("Unable to create notification [" + e.getMessage() + "]");
            return null;
        }
    }
}
