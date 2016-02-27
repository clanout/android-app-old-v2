package reaper.android.app.service._new;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;

import reaper.android.app.model.Location;

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
            return false;
        }
    }

    public void connect()
    {
        if (googleApiClient == null)
        {
            throw new IllegalStateException("[GoogleApiClient not initialized]");
        }

        googleApiClient.connect();
    }

    public GoogleApiClient getGoogleApiClient()
    {
        if (googleApiClient == null)
        {
            throw new IllegalStateException("[GoogleApiClient not initialized]");
        }
        else if (!googleApiClient.isConnected())
        {
            throw new IllegalStateException("[GoogleApiClient not connected]");
        }

        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient)
    {
        if (googleApiClient == null)
        {
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
