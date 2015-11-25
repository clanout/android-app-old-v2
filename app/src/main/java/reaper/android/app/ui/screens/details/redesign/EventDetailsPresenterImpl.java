package reaper.android.app.ui.screens.details.redesign;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventAttendeeComparator;
import reaper.android.app.model.EventDetails;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.app.ui.util.event.EventUtilsConstants;
import reaper.android.common.analytics.AnalyticsHelper;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class EventDetailsPresenterImpl implements EventDetailsPresenter
{
    /* View */
    private EventDetailsView view;

    /* Service */
    private EventService eventService;

    /* Data */
    private Event event;
    private String userId;
    private String userName;
    private String status;
    private int statusType;
    private EventDetails eventDetails;
    private boolean isRsvpUpdateInProgress;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventDetailsPresenterImpl(Bus bus, Event event)
    {
        this.event = event;
        eventService = new EventService(bus);
        subscriptions = new CompositeSubscription();

        UserService userService = new UserService(bus);
        userId = userService.getActiveUserId();
        userName = userService.getActiveUserName();
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
        Timber.v(">>>> STATUS CLICKED");
    }

    @Override
    public void onEdit()
    {
        if (EventUtils.canEdit(event, userId) == EventUtilsConstants.CAN_EDIT)
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
                        ._fetchEventDetailsFromCache(event.getId())
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
                        ._fetchEventDetailsFromNetwork(event.getId())
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
                            }
                        });

        subscriptions.add(subscription);
    }

    private void displayDetails(EventDetails eventDetails)
    {
        this.eventDetails = eventDetails;

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

        view.displayDescription(eventDetails.getDescription());
        view.displayStatusMessage(statusType, status);
        view.displayAttendeeList(attendees);
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
