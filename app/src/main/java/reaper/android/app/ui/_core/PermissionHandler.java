package reaper.android.app.ui._core;

import android.Manifest;
import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;

public class PermissionHandler
{
    @IntDef({Permissions.LOCATION})
    public @interface Permissions
    {
        int LOCATION = 1;
    }

    public static boolean isRationalRequired(Activity activity, @Permissions int permission)
    {
        return ActivityCompat
                .shouldShowRequestPermissionRationale(activity, getPermissionString(permission));
    }

    public static void requestPermission(Activity activity, @Permissions int permission)
    {
        ActivityCompat
                .requestPermissions(activity, new String[]{getPermissionString(permission)}, permission);
    }

    private static String getPermissionString(@Permissions int permission)
    {
        switch (permission)
        {
            case Permissions.LOCATION:
                return Manifest.permission.ACCESS_COARSE_LOCATION;

            default:
                throw new IllegalArgumentException("Invalid Permission");
        }
    }
}
