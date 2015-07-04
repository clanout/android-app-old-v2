package reaper.android.app.cache;

import reaper.android.app.config.CacheKeys;
import reaper.android.app.trigger.common.CacheCommitTrigger;
import reaper.android.common.cache.Cache;
import reaper.android.common.communicator.Communicator;

/**
 * Created by aditya on 02/07/15.
 */
public class UserCache
{
    public static void setSessionCookie(String sessionCookie)
    {
        Cache.getInstance().put(CacheKeys.SESSION_COOKIE, sessionCookie);
        Communicator.getInstance().getBus().post(new CacheCommitTrigger());
    }

    public static String getSessionCookie()
    {
        String sessionCookie = (String) Cache.getInstance().get(CacheKeys.SESSION_COOKIE);
        if(sessionCookie == null)
        {
            UserCache.reset();
            EventCache.reset();
        }
        return sessionCookie;
    }

    public static void setActiveUser(String activeUser)
    {
        Cache.getInstance().put(CacheKeys.ACTIVE_USER, activeUser);
    }

    public static String getActiveUser()
    {
        String activeUser = (String) Cache.getInstance().get(CacheKeys.ACTIVE_USER);
        if(activeUser == null)
        {
            throw new NullPointerException("Active User is null");
        }
        return activeUser;
    }

    public static void setUserLocation(String zone, Double longitude, Double latitude)
    {
        Cache cache = Cache.getInstance();

        String oldZone = (String) cache.get(CacheKeys.USER_LOCATION_ZONE);
        if (oldZone == null)
        {
            throw new NullPointerException("User Location Zone is null");
        }

        if(!oldZone.equals(zone))
        {
            EventCache.reset();
        }

        cache.put(CacheKeys.USER_LOCATION_ZONE, zone);
        cache.put(CacheKeys.USER_LOCATION_LONGITUDE, longitude);
        cache.put(CacheKeys.USER_LOCATION_LATITUDE, latitude);

        Communicator.getInstance().getBus().post(new CacheCommitTrigger());
    }

    public static String getUserLocationZone()
    {
        String zone = (String) Cache.getInstance().get(CacheKeys.USER_LOCATION_ZONE);
        if (zone == null)
        {
            throw new NullPointerException("User Location Zone is null");
        }
        return zone;
    }

    public static double getUserLocationLongitude()
    {
        Double longitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LONGITUDE);
        if(longitude == null)
        {
            throw new NullPointerException("User Location Longitude is null");
        }
        return longitude;
    }

    public static double getUserLocationLatitude()
    {
        Double latitude = (Double) Cache.getInstance().get(CacheKeys.USER_LOCATION_LATITUDE);
        if(latitude == null)
        {
            throw new NullPointerException("User Location Latitude is null");
        }
        return latitude;
    }

    public static void reset()
    {
        Cache cache = Cache.getInstance();

        cache.remove(CacheKeys.SESSION_COOKIE);
        cache.remove(CacheKeys.ACTIVE_USER);
        cache.remove(CacheKeys.USER_LOCATION_ZONE);
        cache.remove(CacheKeys.USER_LOCATION_LONGITUDE);
        cache.remove(CacheKeys.USER_LOCATION_LATITUDE);

        Communicator.getInstance().getBus().post(new CacheCommitTrigger());
    }
}
