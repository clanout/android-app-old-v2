package reaper.android.app.api.event;

import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface EventApi
{
    @POST("/event/summary")
    public void getEvents(@Body EventsApiRequest request, Callback<EventsApiResponse> callback);

    @POST("/event/details")
    public void getEventDetails(@Body EventDetailsApiRequest request, Callback<EventDetailsApiResponse> callback);
}
