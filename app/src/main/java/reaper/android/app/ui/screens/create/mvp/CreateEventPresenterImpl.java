package reaper.android.app.ui.screens.create.mvp;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service._new.LocationService_;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CreateEventPresenterImpl implements CreateEventPresenter
{
    /* Services */
    private EventService eventService;
    private PlacesService placesService;
    private LocationService_ locationService;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    /* View */
    private CreateEventView view;

    /* Data */
    private boolean isAutocompleteInProgress;
    private boolean isLocationFetchInProgress;
    private boolean isCreationInitiated;

    private EventCategory eventCategory;
    private Location location;

    private String title;
    private Event.Type type;
    private String description;
    private DateTime startTime;
    private DateTime endTime;

    public CreateEventPresenterImpl(EventService eventService, LocationService_ locationService, PlacesService placesService)
    {
        this.eventService = eventService;
        this.locationService = locationService;
        this.placesService = placesService;

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
    public void changeCategory(EventCategory category)
    {
        eventCategory = category;
        fetchSuggestions();
    }

    @Override
    public void autocomplete(final String input)
    {
        if (isAutocompleteInProgress)
        {
            return;
        }

        isAutocompleteInProgress = true;

        Location userLocation = locationService.getCurrentLocation();
        Subscription subscription =
                placesService
                        ._autocomplete(userLocation.getLatitude(), userLocation
                                .getLongitude(), input)
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

        final Location userLocation = locationService.getCurrentLocation();
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
        Location userLocation = locationService.getCurrentLocation();
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
            Location userLocation = locationService.getCurrentLocation();
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
                        view.navigateToInviteScreen(event.getId());
                    }
                });

        subscriptions.add(subscription);
    }

    private void fetchSuggestions()
    {
        Location userLocation = locationService.getCurrentLocation();
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
