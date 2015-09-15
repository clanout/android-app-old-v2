package reaper.android.common.notification;

import android.os.Bundle;

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

            Notification notification =
                    new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                            .type(typeCode)
                            .title(TITLE)
                            .eventId(args.get("event_id"))
                            .timestamp(DateTime.now())
                            .message(NotificationHelper.getMessage(typeCode, args))
                            .isNew(true)
                            .build();

            return notification;
        }
        catch (Exception e)
        {
            Timber.e("Unable to create notification [" + e.getMessage() + "]");
            return null;
        }
    }
}
