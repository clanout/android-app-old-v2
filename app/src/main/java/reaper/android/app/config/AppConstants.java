package reaper.android.app.config;

public final class AppConstants
{
    /* Server URLs */
    public static final String BASE_URL_SERVER = "http://api.adityap.com/reaper/v1.0/";
    public static final String BASE_URL_GOOGLE_PLACES_API = "https://maps.googleapis.com/maps/api/place/";
    public static final String BASE_URL_FACEBOOK_API = "https://graph.facebook.com/v2.4/";

    /* API Keys */
    public static final String GOOGLE_API_KEY = "AIzaSyDBX362r-1isovteBR3tGN3QQtDcQn-jyg";
    public static final String GOOGLE_ANALYTICS_TRACKING_KEY = "UA-67721019-1";


    public static final String DEFAULT_COUNTRY_CODE = "91";

    //GCM
    public static final String GCM_SENDER_ID = "1014674558116";

    // Whatsapp Message
    public static final String WHATSAPP_INVITATION_MESSAGE = " wants to clan out with you. Join Harsh here."; //  TODO -- change name

    // App Link
    public static final String APP_LINK = "www.clanout.com"; // TODO -- change link

    public static final int TITLE_LENGTH_LIMIT = 30;

    // Expiry Times
    public static final int EXPIRY_DAYS_EVENT_SUGGESTIONS = 7;

    private AppConstants()
    {
    }
}
