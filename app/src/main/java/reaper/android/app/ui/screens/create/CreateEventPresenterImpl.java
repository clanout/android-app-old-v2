package reaper.android.app.ui.screens.create;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.google.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.model.Suggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.GoogleService;
import reaper.android.app.service.LocationService;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CreateEventPresenterImpl implements CreateEventPresenter
{
    /* Services */
    private EventService eventService;
    private GoogleService googleService;
    private Location userLocation;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    /* View */
    private CreateEventView view;

    /* Data */
    private boolean isAutocompleteInProgress;
    private boolean isLocationFetchInProgress;
    private boolean isCreationInitiated;

    private String title;
    private Event.Type type;
    private EventCategory eventCategory;
    private String description;
    private DateTime startTime;
    private DateTime endTime;
    private Location location;

    public CreateEventPresenterImpl(Bus bus, EventCategory eventCategory)
    {
        eventService = new EventService(bus);
        googleService = new GoogleService(bus);
        userLocation = new LocationService(bus).getUserLocation();
        subscriptions = new CompositeSubscription();
        this.eventCategory = eventCategory;
    }

    @Override
    public void attachView(CreateEventView view)
    {
        this.view = view;

        fetchSuggestions();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void changeCategory(EventCategory category)
    {
        fetchSuggestions();
    }

    @Override
    public void autocomplete(final String s)
    {
        if (isAutocompleteInProgress)
        {
            return;
        }

        isAutocompleteInProgress = true;

        Subscription subscription = Observable
                .create(new Observable.OnSubscribe<List<GooglePlaceAutocompleteApiResponse.Prediction>>()
                {
                    @Override
                    public void call(Subscriber<? super List<GooglePlaceAutocompleteApiResponse.Prediction>> subscriber)
                    {
                        List<GooglePlaceAutocompleteApiResponse.Prediction> predictions = googleService
                                .autocomplete(userLocation.getLatitude(), userLocation
                                        .getLongitude(), s);

                        subscriber.onNext(predictions);
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<List<GooglePlaceAutocompleteApiResponse.Prediction>, List<Suggestion>>()
                {
                    @Override
                    public List<Suggestion> call(List<GooglePlaceAutocompleteApiResponse.Prediction> predictions)
                    {
                        List<Suggestion> suggestions = new ArrayList<Suggestion>();
                        int count = 0;
                        for (GooglePlaceAutocompleteApiResponse.Prediction prediction : predictions)
                        {
                            if (count == 5)
                            {
                                break;
                            }

                            Suggestion suggestion = new Suggestion();
                            suggestion.setId(prediction.getPlaceId());
                            suggestion.setName(prediction.getDescription());
                            suggestions.add(suggestion);
                            count++;
                        }

                        return suggestions;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Suggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        isAutocompleteInProgress = false;
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(List<Suggestion> suggestions)
                    {
                        view.displaySuggestions(suggestions);
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void selectSuggestion(final Suggestion suggestion)
    {
        if (suggestion.getLatitude() != null && suggestion.getLongitude() != null)
        {
            Location location = new Location();
            location.setZone(userLocation.getZone());
            location.setLatitude(suggestion.getLatitude());
            location.setLongitude(suggestion.getLongitude());
            location.setName(suggestion.getName());
            view.setLocation(location);

            isLocationFetchInProgress = false;
        }
        else
        {
            if (suggestion.getId() != null)
            {
                isLocationFetchInProgress = true;

                Subscription subscription = googleService
                        ._getPlaceDetails(suggestion.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Location>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isLocationFetchInProgress = false;

                                if (isCreationInitiated)
                                {
                                    create();
                                }
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                Location location = new Location();
                                location.setZone(userLocation.getZone());
                                location.setName(suggestion.getName());
                                view.setLocation(location);

                                isLocationFetchInProgress = false;

                                if (isCreationInitiated)
                                {
                                    create();
                                }
                            }

                            @Override
                            public void onNext(Location location)
                            {
                                view.setLocation(location);
                            }
                        });

                subscriptions.add(subscription);
            }
            else
            {
                if (view != null)
                {
                    Location location = new Location();
                    location.setZone(userLocation.getZone());
                    location.setName(suggestion.getName());
                    view.setLocation(location);
                }

                isLocationFetchInProgress = false;
            }
        }
    }

    @Override
    public void create(String title, Event.Type type, EventCategory category, String description,
                       DateTime startTime, DateTime endTime, Location location)
    {
        view.showLoading();

        isCreationInitiated = true;

        this.title = title;
        this.type = type;
        this.eventCategory = category;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;

        if (!isLocationFetchInProgress)
        {
            create();
        }
    }

    private void create()
    {
        if (view == null)
        {
            return;
        }

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
            location = new Location();
            location.setZone(userLocation.getZone());
        }

        Subscription subscription = eventService
                ._create(title, type, eventCategory, description, location, startTime, endTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        isCreationInitiated = false;
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        isCreationInitiated = false;
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

    private void fetchSuggestions()
    {
        Subscription subscription = eventService
                ._fetchSuggestions(eventCategory, userLocation.getLatitude(), userLocation
                        .getLongitude())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Suggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displaySuggestions(new ArrayList<Suggestion>());
                    }

                    @Override
                    public void onNext(List<Suggestion> suggestions)
                    {
                        List<Suggestion> suggestionList = new ArrayList<Suggestion>();
                        int count = 0;
                        for (Suggestion suggestion : suggestions)
                        {
                            if (count == 5)
                            {
                                break;
                            }
                            suggestionList.add(suggestion);
                            count++;
                        }
                        view.displaySuggestions(suggestionList);
                    }
                });

        subscriptions.add(subscription);
    }
}
