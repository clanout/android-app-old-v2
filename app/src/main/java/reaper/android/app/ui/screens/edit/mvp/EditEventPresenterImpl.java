package reaper.android.app.ui.screens.edit.mvp;

import org.joda.time.DateTime;

import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.Location;
import reaper.android.app.model.util.DateTimeUtil;
import reaper.android.app.service.EventService;
import reaper.android.common.analytics.AnalyticsHelper;
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
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT, GoogleAnalyticsConstants.ACTION_EDIT_TIME, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */
        }

        if (originalEvent.getLocation().equals(location) || location == null)
        {
            location = new Location();
        }
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT, GoogleAnalyticsConstants.ACTION_EDIT_LOCATION, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */
        }


        if (description.equals(originalEvent.getDescription()))
        {
            description = null;
        }
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT, GoogleAnalyticsConstants.ACTION_EDIT_DESCRIPTION, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */
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
