package reaper.android.app.ui.screens.create;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.google_places.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.config.GoogleAnalyticsConstants;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.common.analytics.AnalyticsHelper;
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
    private PlacesService placesService;
    private Location userLocation;
    private UserService userService;

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
        userService = UserService.getInstance();
        eventService = EventService.getInstance();
        placesService = PlacesService.getInstance();
        userLocation = LocationService_.getInstance().getCurrentLocation();
        subscriptions = new CompositeSubscription();
        this.eventCategory = eventCategory;

        if (this.eventCategory == null)
        {
            this.eventCategory = EventCategory.GENERAL;
        }
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
        eventCategory = category;
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
                        List<GooglePlaceAutocompleteApiResponse.Prediction> predictions = placesService
                                .autocomplete(userLocation.getLatitude(), userLocation
                                        .getLongitude(), s);

                        subscriber.onNext(predictions);
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<List<GooglePlaceAutocompleteApiResponse.Prediction>, List<LocationSuggestion>>()
                {
                    @Override
                    public List<LocationSuggestion> call(List<GooglePlaceAutocompleteApiResponse.Prediction> predictions)
                    {
                        List<LocationSuggestion> locationSuggestions = new ArrayList<LocationSuggestion>();
                        int count = 0;
                        for (GooglePlaceAutocompleteApiResponse.Prediction prediction : predictions)
                        {
                            if (count == 5)
                            {
                                break;
                            }

                            LocationSuggestion locationSuggestion = new LocationSuggestion();
                            locationSuggestion.setId(prediction.getPlaceId());
                            locationSuggestion.setName(prediction.getDescription());
                            locationSuggestions.add(locationSuggestion);
                            count++;
                        }

                        return locationSuggestions;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<LocationSuggestion>>()
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
                    public void onNext(List<LocationSuggestion> suggestions)
                    {
                        view.displaySuggestions(suggestions);
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void selectSuggestion(final LocationSuggestion locationSuggestion)
    {
        isLocationFetchInProgress = true;

        if (locationSuggestion.getLatitude() != null && locationSuggestion.getLongitude() != null)
        {
            location = new Location();
            location.setZone(userLocation.getZone());
            location.setLatitude(locationSuggestion.getLatitude());
            location.setLongitude(locationSuggestion.getLongitude());
            location.setName(locationSuggestion.getName());
            view.setLocation(location.getName());

            isLocationFetchInProgress = false;
            if (isCreationInitiated)
            {
                create();
            }
        }
        else
        {
            if (locationSuggestion.getId() != null)
            {
                Subscription subscription = placesService
                        ._getPlaceDetails(locationSuggestion.getId())
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
                                location = new Location();
                                location.setZone(userLocation.getZone());
                                location.setName(locationSuggestion.getName());
                                view.setLocation(location.getName());

                                isLocationFetchInProgress = false;

                                if (isCreationInitiated)
                                {
                                    create();
                                }
                            }

                            @Override
                            public void onNext(Location location)
                            {
                                CreateEventPresenterImpl.this.location = location;
                                view.setLocation(location.getName());
                            }
                        });

                subscriptions.add(subscription);
            }
            else
            {
                if (view != null)
                {
                    location = new Location();
                    location.setZone(userLocation.getZone());
                    location.setName(locationSuggestion.getName());
                    view.setLocation(location.getName());
                }

                isLocationFetchInProgress = false;
                if (isCreationInitiated)
                {
                    create();
                }
            }
        }
    }

    @Override
    public void setLocationName(String locationName)
    {
        location = new Location();
        location.setZone(userLocation.getZone());
        location.setName(locationName);
    }

    @Override
    public void create(String title, Event.Type type, String description, DateTime startTime, DateTime endTime)
    {
        view.showLoading();

        isCreationInitiated = true;

        this.title = title;
        this.type = type;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;

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
                        AnalyticsHelper
                                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.CREATE_EVENT_FAILURE_FROM_DETAILS, userService
                                        .getSessionUserId());
                        isCreationInitiated = false;
                        view.displayError();
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        AnalyticsHelper
                                .sendEvents(GoogleAnalyticsConstants.GENERAL, GoogleAnalyticsConstants.CREATE_EVENT_SUCCESS_FROM_DETAILS, userService
                                        .getSessionUserId());
                        view.navigateToInviteScreen(event);
                    }
                });

        subscriptions.add(subscription);
    }

    private void fetchSuggestions()
    {
        Subscription subscription = eventService
                ._fetchLocationSuggestions(eventCategory, userLocation.getLatitude(), userLocation
                        .getLongitude())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<LocationSuggestion>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displaySuggestions(new ArrayList<LocationSuggestion>());
                    }

                    @Override
                    public void onNext(List<LocationSuggestion> suggestions)
                    {
                        List<LocationSuggestion> locationSuggestionList = new ArrayList<LocationSuggestion>();
                        int count = 0;
                        for (LocationSuggestion suggestion : suggestions)
                        {
                            if (count == 5)
                            {
                                break;
                            }
                            locationSuggestionList.add(suggestion);
                            count++;
                        }
                        view.displaySuggestions(locationSuggestionList);
                    }
                });

        subscriptions.add(subscription);
    }
}
