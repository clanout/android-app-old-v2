package reaper.android.app.config;

import android.content.res.Resources;

/**
 * Created by Aditya on 15-09-2015.
 */
public class Dimensions
{
    public static final int DP_TO_PX_MULTIPLIER = (int) (Resources.getSystem()
                                                                  .getDisplayMetrics().density);

    /*
     ** CATEGORY ICON
     */

    // Used inside standard 48dp bubble
    public static final int CATEGORY_ICON_DEFAULT = 32;

    // Used in create screen
    public static final int CATEGORY_ICON_LARGE = 48;

    /*
     ** USER PROFILE PIC
     */

    // Used inside standard 48dp bubble
    public static final int PROFILE_PIC_DEFAULT = DP_TO_PX_MULTIPLIER * 48;

    // Used in account screen
    public static final int PROFILE_PIC_LARGE = DP_TO_PX_MULTIPLIER * 100;
}
