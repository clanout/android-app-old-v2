package reaper.android.common.cache;

import android.content.Context;
import android.content.SharedPreferences;

import reaper.android.app.config.AppConstants;

public class AppPreferences
{
    public static String get(Context context, String key)
    {
        try
        {
            String appPreferencesFile = AppConstants.APP_PREFERENCES_FILE;
            SharedPreferences appPreferences = context.getSharedPreferences(appPreferencesFile, Context.MODE_PRIVATE);
            return appPreferences.getString(key, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void set(Context context, String key, String value)
    {
        try
        {
            String appPreferencesFile = AppConstants.APP_PREFERENCES_FILE;
            SharedPreferences appPreferences = context.getSharedPreferences(appPreferencesFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = appPreferences.edit();

            editor.remove(key);
            editor.putString(key, value);
            editor.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
