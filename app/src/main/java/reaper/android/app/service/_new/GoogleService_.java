package reaper.android.app.service._new;

import com.google.android.gms.common.api.GoogleApiClient;

public class GoogleService_
{
    private static GoogleService_ instance;

    public static void init()
    {
        instance = new GoogleService_();
    }

    public static GoogleService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[GoogleService Not Initialized]");
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
}
