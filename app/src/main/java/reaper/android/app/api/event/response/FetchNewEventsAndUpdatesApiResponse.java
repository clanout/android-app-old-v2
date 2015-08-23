package reaper.android.app.api.event.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reaper.android.app.api.core.ApiResponse;
import reaper.android.app.model.Event;

/**
 * Created by Aditya on 23-08-2015.
 */
public class FetchNewEventsAndUpdatesApiResponse extends ApiResponse
{
    @SerializedName("new_events")
    private List<Event> newEventsList;

    @SerializedName("updated_events")
    private List<Event> updatedEventList;

    @SerializedName("deleted_events")
    private List<String> deletedEventIdList;

    public FetchNewEventsAndUpdatesApiResponse(List<Event> newEventsList, List<Event> updatedEventList, List<String> deletedEventIdList)
    {
        this.newEventsList = newEventsList;
        this.updatedEventList = updatedEventList;
        this.deletedEventIdList = deletedEventIdList;
    }

    public List<Event> getNewEventsList()
    {
        return newEventsList;
    }

    public List<Event> getUpdatedEventList()
    {
        return updatedEventList;
    }

    public List<String> getDeletedEventIdList()
    {
        return deletedEventIdList;
    }
}
