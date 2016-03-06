package reaper.android.app.ui.screens.details.mvp;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.util.EventAttendeeComparator;
import reaper.android.app.service.EventService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.EventUtils;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventDetailsPresenterImpl implements EventDetailsPresenter
{
    /* View */
    private EventDetailsView view;

    /* Service */
    private EventService eventService;
    private UserService userService;

    /* Data */
    private Event event;
    private EventDetails eventDetails;
    private String status;
    private boolean isLastMinute;

    private boolean isRsvpUpdateInProgress;
    private boolean isEditClicked;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventDetailsPresenterImpl(EventService eventService, UserService userService, Event event)
    {
        this.eventService = eventService;
        this.userService = userService;
        this.event = event;
        processIsLastMinute();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EventDetailsView view)
    {
        this.view = view;

        initView();
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
        view.navigateToInviteScreen(event.getId());
    }

    @Override
    public void chat()
    {
        view.navigateToChatScreen(event.getId());
    }

    @Override
    public void toggleRsvp()
    {
        final boolean oldRsvp = event.getRsvp() == Event.RSVP.YES;
        if (isRsvpUpdateInProgress)
        {
            view.displayRsvp(oldRsvp, isInvited());
            return;
        }

        if (view == null)
        {
            return;
        }

        isRsvpUpdateInProgress = true;

        view.displayRsvp(!oldRsvp, isInvited());
        if (oldRsvp)
        {
            event.setRsvp(Event.RSVP.NO);
        }
        else
        {
            event.setRsvp(Event.RSVP.YES);
        }

        setEditAction();
        displayStatus(status);

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
                                setEditAction();
                                displayStatus(status);
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

                                view.displayRsvp(oldRsvp, isInvited());
                                view.displayRsvpError();
                                isRsvpUpdateInProgress = false;
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

                                    view.displayRsvp(oldRsvp, isInvited());
                                    view.displayRsvpError();
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void edit()
    {
        isEditClicked = true;

        if (eventDetails != null)
        {
            editEvent();
        }
    }

    @Override
    public void requestEditActionState()
    {
        setEditAction();
    }

    @Override
    public void setStatus(String status)
    {
        if (status == null)
        {
            status = "";
        }

        displayStatus(status);

        if (isLastMinute && !status.isEmpty())
        {
            eventService.updateStatus(event.getId(), status, true);
        }
        else
        {
            eventService.updateStatus(event.getId(), status, false);
        }
    }

    @Override
    public void sendInvitationResponse(String invitationResponse)
    {
        eventService.sendInvitationResponse(event.getId(), invitationResponse);
    }

    @Override
    public String getTitle()
    {
        return DateTimeUtil.getDetailsScreenTitle(event.getStartTime());
    }

    /* Helper Methods */
    private void initView()
    {
        displaySummary(event);

        view.showAttendeeLoading();
        Subscription subscription =
                eventService
                        ._fetchDetailsCache(event.getId())
                        .subscribeOn(Schedulers.newThread())
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
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(EventDetails eventDetails)
                            {
                                if (eventDetails != null)
                                {
                                    displayDetails(eventDetails);
                                }
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
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                            }

                            @Override
                            public void onNext(EventDetails eventDetails)
                            {
                                displayDetails(eventDetails);
                                view.hideAttendeeLoading();
                            }
                        });

        subscriptions.add(subscription);
    }

    private void displaySummary(Event event)
    {
        if (view == null)
        {
            return;
        }

        view.displayEventSummary(event);
        view.displayUserSummary(userService.getSessionUser());

        if (event.getRsvp() == Event.RSVP.YES)
        {
            view.displayRsvp(true, isInvited());
        }
        else
        {
            view.displayRsvp(false, isInvited());
        }

        if (event.getOrganizerId().equals(userService.getSessionUserId()))
        {
            view.disableRsvp();
        }

        setEditAction();
    }

    private void displayDetails(EventDetails eventDetails)
    {
        this.eventDetails = eventDetails;
        view.displayDescription(eventDetails.getDescription());

        List<EventDetails.Attendee> attendees = eventDetails.getAttendees();

        if (event.getRsvp() == Event.RSVP.YES)
        {
            EventDetails.Attendee attendee = new EventDetails.Attendee();
            attendee.setId(userService.getSessionUserId());
            if (attendees.contains(attendee))
            {
                EventDetails.Attendee self = attendees.remove(attendees.indexOf(attendee));
                status = self.getStatus();
            }
        }
        else
        {
            status = null;
        }
        displayStatus(status);

        Collections.sort(attendees, new EventAttendeeComparator(userService.getSessionUserId()));
        view.displayAttendeeList(attendees);

        if (isEditClicked)
        {
            editEvent();
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

    private void displayStatus(String status)
    {
        if (event.getRsvp() == Event.RSVP.YES)
        {
            if (isLastMinute)
            {
                view.displayLastMinuteStatus(status);
            }
            else
            {
                view.displayStatus(status);
            }
        }
        else
        {
            view.hideStatus();
        }
    }

    private void processIsLastMinute()
    {
        isLastMinute = DateTime.now().plusHours(1).isAfter(event.getStartTime());
    }

    private boolean isInvited()
    {
        return event.getInviterCount() > 0;
    }

    private void editEvent()
    {
        isEditClicked = false;
        if (event.isFinalized() && !EventUtils.isOrganiser(event, userService.getSessionUserId()))
        {
            view.displayEventFinalizedMessage();
        }
        else
        {
            view.navigateToEditScreen(event, eventDetails);
        }
    }
}
