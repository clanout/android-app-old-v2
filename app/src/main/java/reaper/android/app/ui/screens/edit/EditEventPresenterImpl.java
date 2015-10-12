package reaper.android.app.ui.screens.edit;

import org.joda.time.DateTime;

import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service.LocationService;
import reaper.android.app.service.UserService;
import reaper.android.app.ui.util.event.EventUtils;
import reaper.android.common.communicator.Communicator;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class EditEventPresenterImpl implements EditEventPresenter
{
    /* Services */
    private EventService eventService;

    /* Data */
    private String activeUser;
    private Location userLocation;
    private Event originalEvent;
    private String originalDescription;

    private DateTime updatedStartTime;
    private Location updatedLocation;
    private String updatedDescription;

    private boolean isTimeUpdated;
    private boolean isLocationUpdated;
    private boolean isDescriptionUpdated;

    private CompositeSubscription subscriptions;

    /* View */
    private EditEventView view;

    public EditEventPresenterImpl(Event originalEvent, EventDetails originalEventDetails)
    {
        eventService = new EventService(Communicator.getInstance().getBus());
        activeUser = new UserService(Communicator.getInstance().getBus()).getActiveUserId();
        userLocation = new LocationService(Communicator.getInstance().getBus()).getUserLocation();

        this.originalEvent = originalEvent;
        this.originalDescription = originalEventDetails.getDescription();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditEventView view)
    {
        this.view = view;
        this.view.init(originalEvent, originalDescription);

        if (EventUtils.canDeleteEvent(originalEvent, activeUser))
        {
            this.view.enableDeleteOption();
        }

        if (EventUtils.canFinaliseEvent(originalEvent, activeUser))
        {
            if (originalEvent.isFinalized())
            {
                this.view.displayUnfinalizationOption();
            }
            else
            {
                this.view.displayFinalizationOption();
            }
        }
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void finalizeEvent()
    {
        Subscription subscription = Observable
                .zip(eventService._finaliseEvent(originalEvent, true),
                        getEventListObservable(),
                        new Func2<Response, List<Event>, List<Event>>()
                        {
                            @Override
                            public List<Event> call(Response response, List<Event> events)
                            {
                                return events;
                            }
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayFinalizationError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        int activePosition = events.indexOf(originalEvent);
                        view.navigateToDetailsScreen(events, activePosition);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void unfinalizeEvent()
    {
        Subscription subscription = Observable
                .zip(eventService._finaliseEvent(originalEvent, false),
                        getEventListObservable(),
                        new Func2<Response, List<Event>, List<Event>>()
                        {
                            @Override
                            public List<Event> call(Response response, List<Event> events)
                            {
                                return events;
                            }
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayUnfinalizationError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        int activePosition = events.indexOf(originalEvent);
                        view.navigateToDetailsScreen(events, activePosition);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void updateTime(DateTime newTime)
    {
        updatedStartTime = newTime;
        isTimeUpdated = true;
    }

    private Observable<List<Event>> getEventListObservable()
    {
        return eventService._fetchEvents(userLocation.getZone());
    }
}
