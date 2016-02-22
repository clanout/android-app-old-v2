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

    public GoogleApiClient getGoogleApiClient()
    {
        if (googleApiClient == null || !googleApiClient.isConnected())
        {
            throw new IllegalStateException("[GoogleApiClient Not Initialized]");
        }

        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient)
    {
        if (googleApiClient == null || !googleApiClient.isConnected())
        {
            throw new IllegalStateException("[GoogleApiClient Not Initialized]");
        }

        this.googleApiClient = googleApiClient;
    }
}
