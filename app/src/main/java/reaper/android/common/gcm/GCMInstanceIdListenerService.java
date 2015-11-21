package reaper.android.common.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.common.analytics.AnalyticsHelper;

public class GCMInstanceIdListenerService extends InstanceIDListenerService
{
    @Override
    public void onTokenRefresh()
    {
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.GCM_TOKEN_REFRESH,"");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
