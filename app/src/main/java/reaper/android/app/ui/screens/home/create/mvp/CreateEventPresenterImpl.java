package reaper.android.app.ui.screens.home.create.mvp;

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
    /* View */
    private CreateEventView view;

    /* Services */
    private EventService eventService;
    private LocationService_ locationService;

    private CompositeSubscription subscriptions;

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
    public void create(String title, EventCategory category, boolean isSecret, DateTime startTime)
    {
        if (view == null)
        {
            return;
        }

        if (title == null || title.isEmpty())
        {
            view.displayEmptyTitleErrorMessage();
            return;
        }

        DateTime now = DateTime.now();
        if (startTime.isBefore(now))
        {
            view.displayInvalidStartTimeErrorMessage();
            return;
        }

        DateTime endTime = DateTimeUtil.getEndTime(startTime);

        Location location = new Location();
        location.setZone(locationService.getCurrentLocation().getZone());

        Event.Type type = isSecret ? Event.Type.INVITE_ONLY : Event.Type.PUBLIC;

        view.showLoading();
        Subscription subscription = eventService
                ._create(title, type, category, "", location, startTime, endTime)
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
                        view.navigateToInviteScreen(event);
                    }
                });

        subscriptions.add(subscription);
    }
}
