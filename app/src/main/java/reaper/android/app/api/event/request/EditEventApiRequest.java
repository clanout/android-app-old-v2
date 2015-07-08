package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import reaper.android.app.api.core.ApiRequest;

public class EditEventApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("is_finalized")
    private String isFinalized;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("location_latitude")
    private String locationLatitude;

    @SerializedName("location_longitude")
    private String locationLongitude;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("location_zone")
    private String locationZone;

    @SerializedName("description")
    private String description;

    public EditEventApiRequest(String locationLongitude, String description, String endTime, String eventId, String isFinalized, String locationLatitude, String locationName, String locationZone, String startTime)
    {
        this.locationLongitude = locationLongitude;
        this.description = description;
        this.endTime = endTime;
        this.eventId = eventId;
        this.isFinalized = isFinalized;
        this.locationLatitude = locationLatitude;
        this.locationName = locationName;
        this.locationZone = locationZone;
        this.startTime = startTime;
    }
}
