package reaper.android.app.api.event;

import reaper.android.app.api.event.request.CreateEventApiRequest;
import reaper.android.app.api.event.request.DeleteEventApiRequest;
import reaper.android.app.api.event.request.EditEventApiRequest;
import reaper.android.app.api.event.request.EventDetailsApiRequest;
import reaper.android.app.api.event.request.LocationSuggestionsApiRequest;
import reaper.android.app.api.event.request.EventsApiRequest;
import reaper.android.app.api.event.request.FetchEventApiRequest;
import reaper.android.app.api.event.request.FetchNewEventsAndUpdatesApiRequest;
import reaper.android.app.api.event.request.FinaliseEventApiRequest;
import reaper.android.app.api.event.request.GetCreateEventSuggestionsApiRequest;
import reaper.android.app.api.event.request.InviteThroughSMSApiRequest;
import reaper.android.app.api.event.request.InviteUsersApiRequest;
import reaper.android.app.api.event.request.RsvpUpdateApiRequest;
import reaper.android.app.api.event.request.SendChatNotificationApiRequest;
import reaper.android.app.api.event.request.SendInvitaionResponseApiRequest;
import reaper.android.app.api.event.request.UpdateStatusApiRequest;
import reaper.android.app.api.event.response.CreateEventApiResponse;
import reaper.android.app.api.event.response.EditEventApiResponse;
import reaper.android.app.api.event.response.EventDetailsApiResponse;
import reaper.android.app.api.event.response.LocationSuggestionsApiResponse;
import reaper.android.app.api.event.response.EventsApiResponse;
import reaper.android.app.api.event.response.FetchEventApiResponse;
import reaper.android.app.api.event.response.FetchNewEventsAndUpdatesApiResponse;
import reaper.android.app.api.event.response.GetCreateEventSuggestionsApiResponse;
import reaper.android.app.api.event.request.FetchPendingInvitesApiRequest;
import reaper.android.app.api.event.response.FetchPendingInvitesApiResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface EventApi
{
    @POST("/event")
    Observable<FetchEventApiResponse> fetchEvent(@Body FetchEventApiRequest request);

    @POST("/event/summary")
    Observable<EventsApiResponse> getEvents(@Body EventsApiRequest request);

    @POST("/event/details")
    Observable<EventDetailsApiResponse> getEventDetails(@Body EventDetailsApiRequest request);

    @POST("/event/updates")
    Observable<FetchNewEventsAndUpdatesApiResponse> fetchNewEventsAndUpdates(@Body FetchNewEventsAndUpdatesApiRequest request);

    @POST("/event/rsvp")
    Observable<Response> updateRsvp(@Body RsvpUpdateApiRequest request);

    @POST("/event/create")
    Observable<CreateEventApiResponse> createEvent(@Body CreateEventApiRequest request);

    @POST("/event/recommendations")
    Observable<LocationSuggestionsApiResponse> getLocationSuggestions(@Body LocationSuggestionsApiRequest request);

    @POST("/event/edit")
    Observable<EditEventApiResponse> editEvent(@Body EditEventApiRequest request);

    @POST("/event/delete")
    Observable<Response> deleteEvent(@Body DeleteEventApiRequest request);

    @POST("/event/invite")
    Observable<Response> inviteFriends(@Body InviteUsersApiRequest request);

    @POST("/event/finalize")
    Observable<Response> finaliseEvent(@Body FinaliseEventApiRequest request);

    @POST("/event/chat")
    Observable<Response> sendChatNotification(@Body SendChatNotificationApiRequest request);

    @POST("/event/phone_invitation")
    Observable<Response> inviteThroughSMS(@Body InviteThroughSMSApiRequest request);

    @POST("/event/invitation_response")
    Observable<Response> sendInvitationResponse(@Body SendInvitaionResponseApiRequest request);

    @POST("/event/status")
    Observable<Response> updateStatus(@Body UpdateStatusApiRequest request);

    @POST("/event/suggestions")
    Observable<GetCreateEventSuggestionsApiResponse> getCreateEventSuggestion(@Body GetCreateEventSuggestionsApiRequest request);

    @POST("/event/pending_invitations")
    Observable<FetchPendingInvitesApiResponse> fetchPendingInvites(@Body FetchPendingInvitesApiRequest request);
}
