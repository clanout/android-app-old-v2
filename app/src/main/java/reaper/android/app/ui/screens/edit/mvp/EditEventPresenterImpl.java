package reaper.android.app.ui.screens.edit.mvp;

import org.joda.time.DateTime;

import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.model.util.DateTimeUtil;
import reaper.android.app.service.EventService;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class EditEventPresenterImpl implements EditEventPresenter
{
    /* Services */
    private EventService eventService;

    /* Data */
    private Event originalEvent;

    private CompositeSubscription subscriptions;

    /* View */
    private EditEventView view;

    public EditEventPresenterImpl(EventService eventService, Event originalEvent)
    {
        this.eventService = eventService;
        this.originalEvent = originalEvent;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditEventView view)
    {
        this.view = view;
        this.view.init(originalEvent);
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void edit(DateTime startTime, Location location, String description)
    {
        DateTime endTime = DateTimeUtil.getEndTime(startTime);
        if (startTime.equals(originalEvent.getStartTime()))
        {
            startTime = null;
            endTime = null;
        }

        if (originalEvent.getLocation().equals(location) || location == null)
        {
            location = new Location();
        }

        if (description.equals(originalEvent.getDescription()))
        {
            description = null;
        }

        Subscription subscription = eventService
                ._editEvent(originalEvent.getId(), startTime, endTime, location, description)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>()
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
                    public void onNext(Integer responseCode)
                    {
                        if (responseCode == 0)
                        {
                            view.navigateToDetailsScreen(originalEvent.getId());
                        }
                        else
                        {
                            view.displayError();
                        }
                    }
                });

        subscriptions.add(subscription);
    }
}
