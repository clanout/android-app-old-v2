package reaper.android.app.ui.screens.home.feed.mvp;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.service.EventService;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventFeedPresenterImpl implements EventFeedPresenter
{
    /* Services */
    private EventService eventService;

    /* Data */
    private List<Event> events;

    /* View */
    private EventFeedView view;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventFeedPresenterImpl(EventService eventService)
    {
        this.eventService = eventService;

        subscriptions = new CompositeSubscription();
        events = new ArrayList<>();
    }

    @Override
    public void attachView(final EventFeedView view)
    {
        this.view = view;

        this.view.showLoading();

        if (events.isEmpty())
        {
            Subscription subscription = eventService
                    ._fetchEvents()
                    .subscribeOn(Schedulers.newThread())
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
                            view.showError();
                        }

                        @Override
                        public void onNext(List<Event> events)
                        {
                            EventFeedPresenterImpl.this.events = events;

                            if (events.isEmpty())
                            {
                                view.showNoEventsMessage();
                            }
                            else
                            {
                                view.showEvents(events);
                            }
                        }
                    });

            subscriptions.add(subscription);
        }
        else
        {
            view.showEvents(events);
        }
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void refreshEvents()
    {
        Subscription subscription = eventService
                ._fetchEventsNetwork()
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
                        view.showError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        EventFeedPresenterImpl.this.events = events;

                        if (events.isEmpty())
                        {
                            view.showNoEventsMessage();
                        }
                        else
                        {
                            view.showEvents(events);
                        }
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void selectEvent(Event event)
    {
        view.gotoDetailsView(event.getId());
    }
}
