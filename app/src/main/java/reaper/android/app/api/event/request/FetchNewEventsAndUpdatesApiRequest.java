package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.List;

import reaper.android.app.api._core.ApiRequest;
import reaper.android.app.api._core.GsonProvider;

/**
 * Created by Aditya on 23-08-2015.
 */
public class FetchNewEventsAndUpdatesApiRequest extends ApiRequest
{
    @SerializedName("zone")
    private String zone;

    @SerializedName("event_list")
    private String eventIdList;

    @SerializedName("last_updated")
    private DateTime lastUpdateTimestamp;

    public FetchNewEventsAndUpdatesApiRequest(String zone, List<String> eventIdList, DateTime lastUpdateTimestamp)
    {
        this.zone = zone;
        this.eventIdList = GsonProvider.getGson().toJson(eventIdList);
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
