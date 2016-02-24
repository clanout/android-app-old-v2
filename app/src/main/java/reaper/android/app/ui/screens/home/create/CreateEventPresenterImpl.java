package reaper.android.app.ui.screens.home.create;

import com.squareup.otto.Bus;

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
    private Location userLocation;

    private CompositeSubscription subscriptions;

    public CreateEventPresenterImpl(Bus bus)
    {
        eventService = EventService.getInstance();
        userLocation = LocationService_.getInstance().getCurrentLocation();

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
        location.setZone(userLocation.getZone());

        Event.Type type = isSecret ? Event.Type.INVITE_ONLY : Event.Type.PUBLIC;

        view.showCreateLoading();
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
                        view.displayCreateFailedMessage();
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
