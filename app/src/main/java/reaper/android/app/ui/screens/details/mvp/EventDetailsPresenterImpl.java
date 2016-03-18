package reaper.android.app.ui.screens.details.mvp;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.util.EventAttendeeComparator;
import reaper.android.app.service.EventService;
import reaper.android.app.service.NotificationService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.DateTimeUtil;
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
    private NotificationService notificationService;

    /* Data */
    private Event event;
    private EventDetails eventDetails;
    private boolean isLastMinute;

    private boolean isRsvpUpdateInProgress;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventDetailsPresenterImpl(EventService eventService, UserService userService,
                                     NotificationService notificationService, Event event)
    {
        this.eventService = eventService;
        this.userService = userService;
        this.notificationService = notificationService;

        this.event = event;
        processIsLastMinute();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EventDetailsView view)
    {
        this.view = view;
        notificationService.deletePlanCreateNotification(event.getId());
        initView();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public String getTitle()
    {
        return DateTimeUtil.getDetailsScreenTitle(event.getStartTime());
    }

    @Override
    public void toggleRsvp()
    {
        if (view == null)
        {
            return;
        }

        final boolean oldRsvp = event.getRsvp() == Event.RSVP.YES;
        if (isRsvpUpdateInProgress)
        {
            view.resetEvent(event);
            return;
        }

        isRsvpUpdateInProgress = true;

        if (oldRsvp)
        {
            event.setRsvp(Event.RSVP.NO);
        }
        else
        {
            event.setRsvp(Event.RSVP.YES);
        }
        view.resetEvent(event);
        processEditState();
        processEventActions();

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

                                view.resetEvent(event);
                                processEditState();
                                processEventActions();
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

                                    view.resetEvent(event);
                                    processEditState();
                                    processEventActions();
                                    isRsvpUpdateInProgress = false;
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void setStatus(String status)
    {
        if (status == null)
        {
            status = "";
        }

        if (!event.getStatus().equals(status))
        {
            event.setStatus(status);
            view.resetEvent(event);

            if (isLastMinute && !status.isEmpty())
            {
                eventService.updateStatus(event.getId(), status, true);
            }
            else
            {
                eventService.updateStatus(event.getId(), status, false);
            }
        }
    }

    @Override
    public void sendInvitationResponse(String invitationResponse)
    {
        eventService.sendInvitationResponse(event.getId(), invitationResponse);
    }

    @Override
    public void invite()
    {
        view.navigateToInvite(event.getId());
    }

    @Override
    public void chat()
    {
        view.navigateToChat(event.getId());
    }

    @Override
    public void edit()
    {
        view.navigateToEdit(event, eventDetails);
    }

    @Override
    public void delete()
    {
        view.showLoading();
        eventService
                ._deleteEvent(event.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {
                        view.navigateToHome();
                        view.hideLoading();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Boolean isSuccessful)
                    {
                        view.navigateToHome();
                        view.hideLoading();
                    }
                });
    }

    /* Helper Methods */
    private void initView()
    {
        view.init(userService.getSessionUser(), event, isLastMinute);
        processEventActions();

        view.showLoading();
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
                                view.hideLoading();

                                processEditState();
                                processDeleteVisibility();
                            }
                        });

        subscriptions.add(subscription);
    }

    private void processEventActions()
    {
        if (event.getRsvp() == Event.RSVP.YES)
        {
            view.displayYayActions();
        }
        else
        {
            view.displayNayActions(event.getInviterCount() > 0);
        }
    }

    private void processDeleteVisibility()
    {
        view.setDeleteVisibility(event.getOrganizerId().equals(userService.getSessionUserId()));
    }

    private void processEditState()
    {
        view.setEditVisibility(event.getRsvp() == Event.RSVP.YES);
    }

    private void displayDetails(EventDetails eventDetails)
    {
        this.eventDetails = eventDetails;
        List<EventDetails.Attendee> attendees = eventDetails.getAttendees();

        if (event.getRsvp() == Event.RSVP.YES)
        {
            EventDetails.Attendee attendee = new EventDetails.Attendee();
            attendee.setId(userService.getSessionUserId());
            attendees.remove(attendee);
        }

        Collections.sort(attendees, new EventAttendeeComparator());
        view.displayAttendees(attendees);
    }

    private void processIsLastMinute()
    {
        isLastMinute = DateTime.now().plusHours(1).isAfter(event.getStartTime());
    }
}
