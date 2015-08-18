package reaper.android.app.api.event;

import reaper.android.app.api.event.request.CreateEventApiRequest;
import reaper.android.app.api.event.request.DeleteEventApiRequest;
import reaper.android.app.api.event.request.EditEventApiRequest;
import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.EventSuggestionsApiRequest;
import reaper.android.app.api.event.request.EventUpdatesApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.request.InviteUsersApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.response.CreateEventApiResponse;
import reaper.android.app.api.event.response.EditEventApiResponse;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.EventSuggestionsApiResponse;
import reaper.android.app.api.event.response.EventUpdatesApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.model.EventDetails;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface EventApi
{
    @POST("/event/summary")
    Observable<EventsApiResponse> getEvents(@Body EventsApiRequest request);

    @POST("/event/details")
    Observable<EventDetailsApiResponse> getEventDetails(@Body EventDetailsApiRequest request);

    @POST("/event/updates")
    void getEventUpdates(@Body EventUpdatesApiRequest request, Callback<EventUpdatesApiResponse> callback);

    @POST("/event/rsvp")
    Observable<Response> updateRsvp(@Body RsvpUpdateApiRequest request);

    @POST("/event/create")
    Observable<CreateEventApiResponse> createEvent(@Body CreateEventApiRequest request);

    @POST("/event/recommendations")
    Observable<EventSuggestionsApiResponse> getEventSuggestions(@Body EventSuggestionsApiRequest request);

    @POST("/event/edit")
    Observable<EditEventApiResponse> editEvent(@Body EditEventApiRequest request);

    @POST("/event/delete")
    Observable<Response> deleteEvent(@Body DeleteEventApiRequest request);

    @POST("/event/invite")
    Observable<Response> inviteFriends(@Body InviteUsersApiRequest request);

    @POST("/event")
    Observable<FetchEventApiResponse> fetchEvent(@Body FetchEventApiRequest request);
}
