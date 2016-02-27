package reaper.android.app.service._new;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

import reaper.android.app.api._core.ApiManager;
import reaper.android.app.api.user.UserApi;
import reaper.android.app.api.user.request.UpdateUserLocationApiRequest;
import reaper.android.app.model.Location;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class LocationService_
{
    private static LocationService_ instance;

    public static void init(Context context, LocationManager locationManager, GoogleService_ googleService)
    {
        if (instance != null)
        {
            return;
        }

        instance = new LocationService_(context, locationManager, googleService);
    }

    public static LocationService_ getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[LocationService Not Initialized]");
        }

        return instance;
    }

    private Context context;
    private LocationManager locationManager;
    private GoogleService_ googleService;
    private UserApi userApi;

    private Location location;

    private LocationListener locationListener;

    private LocationService_(Context context, LocationManager locationManager, GoogleService_ googleService)
    {
        this.context = context;
        this.locationManager = locationManager;
        this.googleService = googleService;
        this.userApi = ApiManager.getUserApi();
    }

    public boolean isLocationPermissionGranted()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationServiceAvailable()
    {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public Observable<Location> fetchCurrentLocation()
    {
        if (location != null)
        {
            return Observable.just(location);
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<android.location.Location>()
                    {
                        @Override
                        public void call(Subscriber<? super android.location.Location> subscriber)
                        {
                            try
                            {
                                //noinspection MissingPermission
                                if (!LocationServices.FusedLocationApi
                                        .getLocationAvailability(googleService.getGoogleApiClient())
                                        .isLocationAvailable())
                                {
                                    Timber.v("[FusedLocationApi] Last Known Location Unavailable");
                                    subscriber.onNext(null);
                                    subscriber.onCompleted();
                                }
                                else
                                {
                                    Timber.v("[FusedLocationApi] Last Known Location Available");
                                    //noinspection MissingPermission
                                    android.location.Location location = LocationServices.FusedLocationApi
                                            .getLastLocation(googleService.getGoogleApiClient());

                                    subscriber.onNext(location);
                                    subscriber.onCompleted();
                                }
                            }
                            catch (Exception e)
                            {
                                subscriber.onError(e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(new Func1<android.location.Location, Observable<android.location.Location>>()
                    {
                        @Override
                        public Observable<android.location.Location> call(android.location.Location location)
                        {
                            if (location != null)
                            {
                                return Observable.just(location);
                            }
                            else
                            {
                                //noinspection MissingPermission
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location == null)
                                {
                                    Timber.v("[LocationManager] Last Known Location Unavailable");
                                }
                                else
                                {
                                    Timber.v("[LocationManager] Last Known Location Available");
                                }

                                return Observable.just(location);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(new Func1<android.location.Location, Observable<android.location.Location>>()
                    {
                        @Override
                        public Observable<android.location.Location> call(android.location.Location location)
                        {
                            if (location != null)
                            {
                                return Observable.just(location);
                            }

                            Timber.v("[FusedLocationApi] Refreshing Location ...");

                            return Observable
                                    .create(new Observable.OnSubscribe<android.location.Location>()
                                    {
                                        @Override
                                        public void call(final Subscriber<? super android.location.Location> subscriber)
                                        {
                                            LocationRequest locationRequest = new LocationRequest();
                                            locationRequest
                                                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                                            locationListener = new LocationListener()
                                            {
                                                @Override
                                                public void onLocationChanged(android.location.Location location)
                                                {
                                                    Timber.v("[FusedLocationApi] Received Updated Location");
                                                    subscriber.onNext(location);
                                                    subscriber.onCompleted();
                                                }
                                            };

                                            //noinspection MissingPermission
                                            LocationServices.FusedLocationApi
                                                    .requestLocationUpdates(googleService
                                                            .getGoogleApiClient(), locationRequest, locationListener);
                                        }
                                    });
                        }
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .map(new Func1<android.location.Location, Location>()
                    {
                        @Override
                        public Location call(android.location.Location googleApiLocation)
                        {
                            if (locationListener != null)
                            {
                                LocationServices.FusedLocationApi
                                        .removeLocationUpdates(googleService
                                                .getGoogleApiClient(), locationListener);

                                Timber.v("[FusedLocationApi] Closed Location Listener");
                            }

                            try
                            {
                                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = null;
                                addresses = gcd
                                        .getFromLocation(googleApiLocation
                                                .getLatitude(), googleApiLocation
                                                .getLongitude(), 1);

                                if (addresses == null || addresses.size() == 0)
                                {
                                    throw new IllegalStateException();
                                }

                                String zone = addresses.get(0).getLocality();

                                Location location = new Location();
                                location.setLongitude(googleApiLocation.getLongitude());
                                location.setLatitude(googleApiLocation.getLatitude());
                                location.setZone(zone);

                                return location;
                            }
                            catch (Exception e)
                            {
                                Timber.e("[Location fetch error] " + e.getMessage());
                                return null;
                            }
                        }
                    })
                    .doOnNext(new Action1<Location>()
                    {
                        @Override
                        public void call(Location location)
                        {
                            if (location != null)
                            {
                                Timber.v("Location : " + location.toString());
                            }

                            LocationService_.this.location = location;
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    public Location getCurrentLocation()
    {
        return location;
    }

    public Observable<Boolean> pushUserLocation()
    {
        if (location == null)
        {
            return Observable.error(new IllegalStateException("Location Null"));
        }

        UpdateUserLocationApiRequest request = new UpdateUserLocationApiRequest(location.getZone());
        return userApi.updateUserLocation(request)
                      .map(new Func1<Response, Boolean>()
                    {
                        @Override
                        public Boolean call(Response response)
                        {
                            return true;
                        }
                    })
                      .onErrorReturn(new Func1<Throwable, Boolean>()
                    {
                        @Override
                        public Boolean call(Throwable e)
                        {
                            Timber.v("[Failed to push updated location] " + e.getMessage());
                            return false;
                        }
                    })
                      .subscribeOn(Schedulers.newThread());
    }
}
