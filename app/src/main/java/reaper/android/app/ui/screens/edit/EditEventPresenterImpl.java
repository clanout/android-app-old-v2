package reaper.android.app.ui.screens.edit;

import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import reaper.android.app.api.google_places.response.GooglePlaceAutocompleteApiResponse;
import reaper.android.app.config.ExceptionMessages;
import reaper.android.app.model.Event;
import reaper.android.app.model.EventCategory;
import reaper.android.app.model.EventDetails;
import reaper.android.app.model.Location;
import reaper.android.app.model.LocationSuggestion;
import reaper.android.app.service.EventService;
import reaper.android.app.service.PlacesService;
import reaper.android.app.service.UserService;
import reaper.android.app.service._new.LocationService_;
import reaper.android.app.ui.util.DateTimeUtil;
import reaper.android.app.ui.util.EventUtils;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class EditEventPresenterImpl implements EditEventPresenter
{
    /* Services */
    private EventService eventService;
    private PlacesService placesService;

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

    private boolean isAutocompleteInProgress;
    private boolean isLocationFetchInProgress;
    private boolean isEditInitiated;

    private CompositeSubscription subscriptions;

    /* View */
    private EditEventView view;

    public EditEventPresenterImpl(Bus bus, Event originalEvent, EventDetails originalEventDetails)
    {
        eventService = EventService.getInstance();
        activeUser = UserService.getInstance().getSessionUserId();
        userLocation = LocationService_.getInstance().getCurrentLocation();
        placesService = new PlacesService();

        this.originalEvent = originalEvent;
        this.originalDescription = originalEventDetails.getDescription();

        Timber.v("FINALIZED = " + originalEvent.isFinalized());

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditEventView view)
    {
        this.view = view;
        this.view.init(originalEvent, originalDescription);


        if (EventUtils.canDeleteEvent(originalEvent, activeUser))
        {
            this.view.displayDeleteOption();
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

        fetchSuggestions();
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
                        view.displayError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        int activePosition = events.indexOf(originalEvent);
                        events.get(activePosition).setIsFinalized(true);
                        view.navigateToDetailsScreen(events, activePosition);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void unfinalizeEvent()
    {
        Subscription subscription = eventService
                ._finaliseEvent(originalEvent, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {

                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void delete()
    {
        Subscription subscription = eventService
                ._deleteEvent(originalEvent.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {
                        view.navigateToHomeScreen();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Response response)
                    {

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
    public void fetchSuggestions()
    {
        Subscription subscription = eventService
                ._fetchSuggestions(EventCategory.valueOf(originalEvent.getCategory()), userLocation
                        .getLatitude(), userLocation
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

    @Override
    public void selectSuggestion(final LocationSuggestion locationSuggestion)
    {
        isLocationFetchInProgress = true;

        if (locationSuggestion.getLatitude() != null && locationSuggestion.getLongitude() != null)
        {
            isLocationUpdated = true;

            updatedLocation = new Location();
            updatedLocation.setZone(userLocation.getZone());
            updatedLocation.setLatitude(locationSuggestion.getLatitude());
            updatedLocation.setLongitude(locationSuggestion.getLongitude());
            updatedLocation.setName(locationSuggestion.getName());
            view.setLocation(updatedLocation.getName());

            isLocationFetchInProgress = false;
            if (isEditInitiated)
            {
                editEvent();
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

                                if (isEditInitiated)
                                {
                                    editEvent();
                                }
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                isLocationUpdated = true;

                                updatedLocation = new Location();
                                updatedLocation.setZone(userLocation.getZone());
                                updatedLocation.setName(locationSuggestion.getName());
                                view.setLocation(updatedLocation.getName());

                                isLocationFetchInProgress = false;

                                if (isEditInitiated)
                                {
                                    editEvent();
                                }
                            }

                            @Override
                            public void onNext(Location location)
                            {
                                isLocationUpdated = true;

                                updatedLocation = location;
                                view.setLocation(location.getName());
                            }
                        });

                subscriptions.add(subscription);
            }
            else
            {
                if (view != null)
                {
                    isLocationUpdated = true;

                    updatedLocation = new Location();
                    updatedLocation.setZone(userLocation.getZone());
                    updatedLocation.setName(locationSuggestion.getName());
                    view.setLocation(updatedLocation.getName());
                }

                isLocationFetchInProgress = false;
                if (isEditInitiated)
                {
                    editEvent();
                }
            }
        }
    }

    @Override
    public void setLocationName(String locationName)
    {
        isLocationUpdated = true;
        updatedLocation = new Location();
        updatedLocation.setZone(userLocation.getZone());
        updatedLocation.setName(locationName);
    }

    @Override
    public void edit()
    {
        view.showLoading();

        isEditInitiated = true;

        if (!isLocationFetchInProgress)
        {
            editEvent();
        }
    }

    @Override
    public void setDescription(String description)
    {
        isDescriptionUpdated = true;
        updatedDescription = description;
    }

    private void editEvent()
    {
        DateTime startTime = null;
        DateTime endTime = null;
        if (isTimeUpdated)
        {
            startTime = updatedStartTime;
            endTime = DateTimeUtil.getEndTime(startTime);
        }

        Location location = new Location();
        if (isLocationUpdated)
        {
            location = updatedLocation;
        }

        String description = originalDescription;
        if (isDescriptionUpdated)
        {
            description = updatedDescription;
        }


        final int[] activePosition = {0};
        Subscription subscription = eventService
                ._editEvent(originalEvent.getId(), startTime, endTime, location, description)
                .map(new Func1<Event, List<Event>>()
                {
                    @Override
                    public List<Event> call(Event event)
                    {
                        List<Event> events = getEventListObservable().toBlocking().first();
                        activePosition[0] = events.indexOf(event);
                        events.set(activePosition[0], event);
                        return events;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        isEditInitiated = false;
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        if (e.getMessage().equals(ExceptionMessages.EVENT_LOCKED))
                        {
                            view.displayEventLockedError();
                        }
                        else
                        {
                            view.displayError();
                        }
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        view.navigateToDetailsScreen(events, activePosition[0]);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void initiateEventDetailsNavigation()
    {
        Subscription subscription =
                getEventListObservable()
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

    private Observable<List<Event>> getEventListObservable()
    {
        return eventService._fetchEvents();
    }
}
