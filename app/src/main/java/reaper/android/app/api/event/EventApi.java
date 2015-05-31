package reaper.android.app.api.event;

import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventUpdatesApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventUpdatesApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface EventApi
{
    @POST("/event/summary")
    public void getEvents(@Body EventsApiRequest request, Callback<EventsApiResponse> callback);

    @POST("/event/details")
    public void getEventDetails(@Body EventDetailsApiRequest request, Callback<EventDetailsApiResponse> callback);

    @POST("/event/updates")
    public void getEventUpdates(@Body EventUpdatesApiRequest request, Callback<EventUpdatesApiResponse> callback);

    @POST("/event/rsvp")
    public void updateRsvp(@Body RsvpUpdateApiRequest request, Callback<Response> callback);
}
