package reaper.android.app.ui.screens.create.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.service.EventService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui.util.DateTimeUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class CreateEventPresenterImpl implements CreateEventPresenter
{
    /* Services */
    private EventService eventService;
    private LocationService_ locationService;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    /* View */
    private CreateEventView view;

    public CreateEventPresenterImpl(EventService eventService, LocationService_ locationService)
    {
        this.eventService = eventService;
        this.locationService = locationService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(CreateEventView view)
    {
        this.view = view;
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void create(String title, Event.Type type, EventCategory category, String description,
                       DateTime startTime, Location location)
    {
        if (view == null)
        {
            return;
        }

        view.showLoading();
        if (title == null || title.isEmpty())
        {
            view.displayEmptyTitleError();
            return;
        }

        DateTime now = DateTime.now();
        if (startTime.isBefore(now))
        {
            view.displayInvalidTimeError();
            return;
        }

        if (location == null)
        {
            Location userLocation = locationService.getCurrentLocation();
            location = new Location();
            location.setZone(userLocation.getZone());
        }

        DateTime endTime = DateTimeUtil.getEndTime(startTime);

        Subscription subscription = eventService
                ._create(title, type, category, description, location, startTime, endTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        view.navigateToInviteScreen(event.getId());
                    }
                });

        subscriptions.add(subscription);
    }
}
