package reaper.android.app.ui.screens.details;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reaper.android.app.cache._core.CacheManager;
import reaper.android.app.cache.event.EventCache;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.util.EventAttendeeComparator;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.EventUtils;
import reaper.android.common.analytics.AnalyticsHelper;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class EventDetailsPresenterImpl implements EventDetailsPresenter
{
    private static final int DISPLAY_LAST_MINUTE_DIALOG = 1;

    /* View */
    private EventDetailsView view;

    /* Service */
    private EventCache eventCache;
    private EventService eventService;

    /* Data */
    private int flag;
    private Event event;
    private String userId;
    private String userName;
    private String status;
    private int statusType;
    private EventDetails eventDetails;
    private boolean isRsvpUpdateInProgress;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventDetailsPresenterImpl(Bus bus, Event event, int flag)
    {
        this.flag = flag;
        this.event = event;
        eventService = EventService.getInstance();
        eventCache = CacheManager.getEventCache();
        subscriptions = new CompositeSubscription();

        UserService userService = UserService.getInstance();
        userId = userService.getSessionUserId();
        userName = userService.getSessionUserName();
    }

    @Override
    public void attachView(EventDetailsView view)
    {
        this.view = view;

        displaySummary();
        fetchEventDetails();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void invite()
    {
        view.navigateToInviteScreen(event);
    }

    @Override
    public void chat()
    {
        view.navigateToChatScreen(event.getId(), event.getTitle());
    }

    @Override
    public void setStatus(String status)
    {
        Timber.v(">>>> qwerty1234 : " + status);
        this.status = status;
        processStatusType();
        view.displayStatusMessage(statusType, status);

        switch (statusType)
        {
            case StatusType.INVITED:
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.GENERAL,
                                GoogleAnalyticsConstants.INVITATION_RESPONSE_SENT,
                                userId);
                eventService.sendInvitationResponse(event.getId(), status);
                break;

            case StatusType.EMPTY:
            case StatusType.NORMAL:
            case StatusType.LAST_MINUTE_EMPTY:
            case StatusType.LAST_MINUTE:
                boolean shouldNotifyFriends = (statusType == StatusType.LAST_MINUTE_EMPTY
                        || statusType == StatusType.LAST_MINUTE);
                eventService.updateStatus(event.getId(), status, shouldNotifyFriends);

                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.GENERAL,
                        GoogleAnalyticsConstants.STATUS_UPDATED, userId);

                break;
        }
    }

    @Override
    public void toggleRsvp()
    {
        final boolean oldRsvp = event.getRsvp() == Event.RSVP.YES;
        if (isRsvpUpdateInProgress)
        {
            view.displayRsvp(oldRsvp);
            return;
        }

        if (view == null)
        {
            return;
        }

        isRsvpUpdateInProgress = true;

        view.displayRsvp(!oldRsvp);
        if (oldRsvp)
        {
            event.setRsvp(Event.RSVP.NO);
        }
        else
        {
            event.setRsvp(Event.RSVP.YES);
        }

        processStatusType();
        setEditAction();
        view.displayStatusMessage(statusType, status);

        Subscription subscription =
                eventService
                        ._updateRsvp(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isRsvpUpdateInProgress = false;
                                processStatusType();
                                setEditAction();

                                view.displayStatusMessage(statusType, status);
                                AnalyticsHelper
                                        .sendEvents(GoogleAnalyticsConstants.GENERAL,
                                                GoogleAnalyticsConstants.RSVP_UPDATED,
                                                "user:" + userId +
                                                        ";event:" + event.getId() +
                                                        ";rsvp:" + event.getRsvp().toString());
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                if (oldRsvp)
                                {
                                    event.setRsvp(Event.RSVP.YES);
                                }
                                else
                                {
                                    event.setRsvp(Event.RSVP.NO);
                                }

                                view.displayRsvp(oldRsvp);
                                view.displayRsvpError();
                                isRsvpUpdateInProgress = false;

                                processStatusType();
                                view.displayStatusMessage(statusType, status);
                            }

                            @Override
                            public void onNext(Boolean isSuccess)
                            {
                                if (!isSuccess)
                                {
                                    if (oldRsvp)
                                    {
                                        event.setRsvp(Event.RSVP.YES);
                                    }
                                    else
                                    {
                                        event.setRsvp(Event.RSVP.NO);
                                    }

                                    view.displayRsvp(oldRsvp);
                                    view.displayRsvpError();
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void onStatusClicked()
    {
        switch (statusType)
        {
            case StatusType.NONE:
                // Not Going; No Invitation
                break;

            case StatusType.INVITED:
                // Not Going; Invited
                view.displayInvitationResponseDialog(event.getId(), userId);
                break;

            case StatusType.EMPTY:
                // Going; Status empty; Add status
                view.displayUpdateStatusDialog(event.getId(), userId, status, false);
                break;

            case StatusType.NORMAL:
                // Going; Status already provided; Update
                view.displayUpdateStatusDialog(event.getId(), userId, status, false);
                break;

            case StatusType.LAST_MINUTE_EMPTY:
                // Going; Status Empty; Add last minute info
                view.displayUpdateStatusDialog(event.getId(), userId, status, true);
                break;

            case StatusType.LAST_MINUTE:
                // Going; Status already provided; Update last minute info
                view.displayUpdateStatusDialog(event.getId(), userId, status, true);
                break;
        }
    }

    @Override
    public void onEdit()
    {
        if (EventUtils.isOrganiser(event, userId))
        {
            view.navigateToEditScreen(event, eventDetails);
        }
        else
        {
            view.displayEventFinalizedMessage();
        }
    }

    @Override
    public void requestEditActionState()
    {
        setEditAction();
    }

    private void displaySummary()
    {
        view.displayEventSummary(event);
        view.displayUserSummary(userId, userName);

        if (event.getRsvp() == Event.RSVP.YES)
        {
            view.displayRsvp(true);
        }
        else
        {
            view.displayRsvp(false);
        }

        if (event.getOrganizerId().equals(userId))
        {
            view.disableRsvp();
        }
    }

    private void fetchEventDetails()
    {
        if (view == null)
        {
            return;
        }

        view.showAttendeeLoading();

        Subscription subscription =
                eventService
                        ._fetchDetailsCache(event.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<EventDetails>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                fetchEventDetailsFromNetwork();
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                fetchEventDetailsFromNetwork();
                            }

                            @Override
                            public void onNext(EventDetails eventDetails)
                            {
                                displayDetails(eventDetails);
                            }
                        });

        subscriptions.add(subscription);
    }

    private void fetchEventDetailsFromNetwork()
    {
        Subscription subscription =
                eventService
                        ._fetchDetailsNetwork(event.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<EventDetails>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                view.hideAttendeeLoading();
                                setEditAction();
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                            }

                            @Override
                            public void onNext(EventDetails eventDetails)
                            {
                                displayDetails(eventDetails);

                                List<String> friends = new ArrayList<>();
                                int friendCount = 0;
                                for (EventDetails.Attendee attendee : eventDetails.getAttendees())
                                {
                                    if (attendee.isFriend())
                                    {
                                        friendCount++;

                                        try
                                        {
                                            String name = attendee.getName();
                                            String[] tokens = name.split(" ");
                                            friends.add(tokens[0]);
                                        }
                                        catch (Exception e)
                                        {
                                        }
                                    }
                                }

                                event.setFriendCount(friendCount);
                                event.setFriends(friends);

                                eventCache.save(event);
                            }
                        });

        subscriptions.add(subscription);
    }

    private void displayDetails(EventDetails eventDetails)
    {
        Timber.v(">>>> DISPLAY DETAILS : " + event.getTitle() + " : " + eventDetails
                .getDescription());

        this.eventDetails = eventDetails;
        view.displayDescription(eventDetails.getDescription());

        List<EventDetails.Attendee> attendees = eventDetails.getAttendees();

        if (event.getRsvp() == Event.RSVP.YES)
        {
            EventDetails.Attendee attendee = new EventDetails.Attendee();
            attendee.setId(userId);
            EventDetails.Attendee self = attendees.remove(attendees.indexOf(attendee));
            status = self.getStatus();
        }
        else
        {
            status = null;
        }
        processStatusType();

        Collections.sort(attendees, new EventAttendeeComparator(userId));

        view.displayStatusMessage(statusType, status);
        view.displayAttendeeList(attendees);


        if (flag == DISPLAY_LAST_MINUTE_DIALOG)
        {
            if (statusType == StatusType.LAST_MINUTE || statusType == StatusType.LAST_MINUTE_EMPTY)
            {
                view.displayUpdateStatusDialog(event.getId(), userId, status, true);
            }
        }
    }

    private void processStatusType()
    {
        if (event.getRsvp() == Event.RSVP.YES)
        {
            DateTime now = DateTime.now();
            if (now.plusHours(1).isAfter(event.getStartTime()))
            {
                if (status == null || status.isEmpty())
                {
                    statusType = StatusType.LAST_MINUTE_EMPTY;
                }
                else
                {
                    statusType = StatusType.LAST_MINUTE;
                }
            }
            else
            {
                if (status == null || status.isEmpty())
                {
                    statusType = StatusType.EMPTY;
                }
                else
                {
                    statusType = StatusType.NORMAL;
                }
            }
        }
        else
        {
            status = null;

            if (event.getInviterCount() > 0)
            {
                statusType = StatusType.INVITED;
            }
            else
            {
                statusType = StatusType.NONE;
            }
        }
    }

    private void setEditAction()
    {
        if (event.getRsvp() == Event.RSVP.YES)
        {
            view.setEditActionState(true);
        }
        else
        {
            view.setEditActionState(false);
        }
    }
}
