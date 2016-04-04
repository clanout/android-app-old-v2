package reaper.android.app.service._new;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Location;
import reaper.android.common.analytics.AnalyticsHelper;

public class GoogleService_
{
    private static GoogleService_ instance;

    public static GoogleService_ getInstance()
    {
        if (instance == null)
        {
            instance = new GoogleService_();
        }

        return instance;
    }

    private GoogleApiClient googleApiClient;

    public boolean isGoogleApiClientSet()
    {
        return googleApiClient != null;
    }

    public boolean isConnected()
    {
        try
        {
            return googleApiClient.isConnected();
        }
        catch (Exception e)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_J, false);
            /* Analytics */

            return false;
        }
    }

    public void connect()
    {
        if (googleApiClient == null)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z2, true);
            /* Analytics */

            throw new IllegalStateException("[GoogleApiClient not initialized]");
        }

        googleApiClient.connect();
    }

    public GoogleApiClient getGoogleApiClient()
    {
        if (googleApiClient == null)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z3, true);
            /* Analytics */
            throw new IllegalStateException("[GoogleApiClient not initialized]");
        }
        else if (!googleApiClient.isConnected())
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z4, true);
            /* Analytics */
            throw new IllegalStateException("[GoogleApiClient not connected]");
        }

        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient)
    {
        if (googleApiClient == null)
        {
             /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z5, true);
            /* Analytics */
            throw new IllegalStateException("[Cannot set null GoogleApiClient]");
        }

        this.googleApiClient = googleApiClient;
    }

    public Intent getGoogleMapsIntent(Location location)
    {
        return new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="
                        + location.getLatitude() + "," + location.getLongitude()));
    }
}
