package reaper.android.app.cache;

import android.support.v4.util.LruCache;

import reaper.android.app.model.EventDetails;

/**
 * Created by aditya on 02/07/15.
 */
public class EventCache
{
    public static void reset()
    {
        LruCache<String, EventDetails> eventDetailsLruCache = new LruCache<>(9);
    }
}
