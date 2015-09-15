package reaper.android.common.analytics;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.root.Reaper;

/**
 * Created by Aditya on 15-09-2015.
 */
public class AnalyticsHelper
{
    private static Tracker googleAnalyticsTracker = Reaper.getAnalyticsTracker();

    public AnalyticsHelper()
    {
    }

    public static void sendScreenNames(String screenName)
    {
        googleAnalyticsTracker.setScreenName(screenName);
        googleAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendEvents(String category, String action, String label)
    {
        googleAnalyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}