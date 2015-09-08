package reaper.android.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import reaper.android.app.api.core.ApiRequest;

public class EditEventApiRequest extends ApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    @SerializedName("start_time")
    private DateTime startTime;

    @SerializedName("end_time")
    private DateTime endTime;

    @SerializedName("location_latitude")
    private Double locationLatitude;

    @SerializedName("location_longitude")
    private Double locationLongitude;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("location_zone")
    private String locationZone;

    @SerializedName("description")
    private String description;

    public EditEventApiRequest(Double locationLongitude, String description, DateTime endTime, String eventId, Double locationLatitude, String locationName, String locationZone, DateTime startTime)
    {
        this.locationLongitude = locationLongitude;
        this.description = description;
        this.endTime = endTime;
        this.eventId = eventId;
        this.locationLatitude = locationLatitude;
        this.locationName = locationName;
        this.locationZone = locationZone;
        this.startTime = startTime;
    }
}
