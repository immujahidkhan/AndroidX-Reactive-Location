package com.android.locationlibrary;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.android.locationlibrary.observables.GoogleAPIClientObservableOnSubscribe;
import com.android.locationlibrary.observables.ObservableContext;
import com.android.locationlibrary.observables.ObservableFactory;
import com.android.locationlibrary.observables.PendingResultObservableOnSubscribe;
import com.android.locationlibrary.observables.activity.ActivityUpdatesObservableOnSubscribe;
import com.android.locationlibrary.observables.geocode.GeocodeObservable;
import com.android.locationlibrary.observables.geocode.ReverseGeocodeObservable;
import com.android.locationlibrary.observables.geofence.AddGeofenceObservableOnSubscribe;
import com.android.locationlibrary.observables.geofence.RemoveGeofenceObservableOnSubscribe;
import com.android.locationlibrary.observables.location.AddLocationIntentUpdatesObservableOnSubscribe;
import com.android.locationlibrary.observables.location.LastKnownLocationObservableOnSubscribe;
import com.android.locationlibrary.observables.location.LocationUpdatesObservableOnSubscribe;
import com.android.locationlibrary.observables.location.MockLocationObservableOnSubscribe;
import com.android.locationlibrary.observables.location.RemoveLocationIntentUpdatesObservableOnSubscribe;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


/**
 * Factory of observables that can manipulate location
 * delivered by Google Play Services.
 */
public class ReactiveLocationProvider {
    private final ObservableContext ctx;
    private final ObservableFactory factory;

    /**
     * Creates location provider instance with default configuration.
     *
     * @param ctx preferably application context
     */
    public ReactiveLocationProvider(Context ctx) {
        this(ctx, ReactiveLocationProviderConfiguration.builder().build());
    }

    /**
     * Create location provider with given {@link ReactiveLocationProviderConfiguration}.
     *
     * @param ctx           preferably application context
     * @param configuration configuration instance
     */
    public ReactiveLocationProvider(Context ctx, ReactiveLocationProviderConfiguration configuration) {
        this.ctx = new ObservableContext(ctx, configuration);
        this.factory = new ObservableFactory(this.ctx);
    }

    /**
     * Creates location provider with custom handler in which all GooglePlayServices callbacks are called.
     *
     * @param ctx     preferably application context
     * @param handler on which all GooglePlayServices callbacks are called
     * @see com.google.android.gms.common.api.GoogleApiClient.Builder#setHandler(Handler)
     * @deprecated please use {@link ReactiveLocationProvider#ReactiveLocationProvider(Context, ReactiveLocationProviderConfiguration)}
     */
    @Deprecated
    public ReactiveLocationProvider(Context ctx, Handler handler) {
        this(ctx, ReactiveLocationProviderConfiguration.builder().setCustomCallbackHandler(handler).build());
    }


    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Observable<Location> getLastKnownLocation() {
        return LastKnownLocationObservableOnSubscribe.createObservable(ctx, factory);
    }

    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Observable<Location> getUpdatedLocation(LocationRequest locationRequest) {
        return LocationUpdatesObservableOnSubscribe.createObservable(ctx, factory, locationRequest);
    }

    @RequiresPermission(
            allOf = {"android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_MOCK_LOCATION"}
    )
    public Observable<Status> mockLocation(Observable<Location> sourceLocationObservable) {
        return MockLocationObservableOnSubscribe.createObservable(ctx, factory, sourceLocationObservable);
    }


    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Observable<Status> requestLocationUpdates(LocationRequest locationRequest, PendingIntent intent) {
        return AddLocationIntentUpdatesObservableOnSubscribe.createObservable(ctx, factory, locationRequest, intent);
    }

    public Observable<Status> removeLocationUpdates(PendingIntent intent) {
        return RemoveLocationIntentUpdatesObservableOnSubscribe.createObservable(ctx, factory, intent);
    }

    /**
     * Creates observable that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. In case geocoder fails with IOException("Service not Available") fallback
     * decoder is used using google web api. You should subscribe for this observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return observable that serves list of address based on location
     */
    public Observable<List<Address>> getReverseGeocodeObservable(double lat, double lng, int maxResults) {
        return ReverseGeocodeObservable.createObservable(ctx.getContext(), factory, Locale.getDefault(), lat, lng, maxResults);
    }

    /**
     * Creates observable that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. In case geocoder fails with IOException("Service not Available") fallback
     * decoder is used using google web api. You should subscribe for this observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param locale     locale for address language
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return observable that serves list of address based on location
     */
    public Observable<List<Address>> getReverseGeocodeObservable(Locale locale, double lat, double lng, int maxResults) {
        return ReverseGeocodeObservable.createObservable(ctx.getContext(), factory, locale, lat, lng, maxResults);
    }

    /**
     * Creates observable that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe for this
     * observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults   max number of results you are interested in
     * @return observable that serves list of address based on location name
     */
    public Observable<List<Address>> getGeocodeObservable(String locationName, int maxResults) {
        return getGeocodeObservable(locationName, maxResults, null);
    }

    /**
     * Creates geocoder with default Locale.
     *
     * @see ReactiveLocationProvider#getGeocodeObservable(String, int, LatLngBounds, Locale)
     */
    public Observable<List<Address>> getGeocodeObservable(String locationName, int maxResults, LatLngBounds bounds) {
        return getGeocodeObservable(locationName, maxResults, bounds, null);
    }

    /**
     * Creates observable that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe for this
     * observable on I/O thread.
     * The stream finishes after address list is available.
     * <p/>
     * You may specify a bounding box for the search results.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults   max number of results you are interested in
     * @param bounds       restricts the results to geographical bounds. May be null
     * @param locale       locale passed to geocoder
     * @return observable that serves list of address based on location name
     */
    public Observable<List<Address>> getGeocodeObservable(String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        return GeocodeObservable.createObservable(ctx.getContext(), factory, locationName, maxResults, bounds, locale);
    }


    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public Observable<Status> addGeofences(PendingIntent geofenceTransitionPendingIntent, GeofencingRequest request) {
        return AddGeofenceObservableOnSubscribe.createObservable(ctx, factory, request, geofenceTransitionPendingIntent);
    }


    public Observable<Status> removeGeofences(PendingIntent pendingIntent) {
        return RemoveGeofenceObservableOnSubscribe.createObservable(ctx, factory, pendingIntent);
    }

    public Observable<Status> removeGeofences(List<String> requestIds) {
        return RemoveGeofenceObservableOnSubscribe.createObservable(ctx, factory, requestIds);
    }


    /**
     * Observable that can be used to observe activity provided by Actity Recognition mechanism.
     *
     * @param detectIntervalMiliseconds detecion interval
     * @return observable that provides activity recognition
     */
    public Observable<ActivityRecognitionResult> getDetectedActivity(int detectIntervalMiliseconds) {
        return ActivityUpdatesObservableOnSubscribe.createObservable(ctx, factory, detectIntervalMiliseconds);
    }

    /**
     * Observable that can be used to check settings state for given location request.
     *
     * @param locationRequest location request
     * @return observable that emits check result of location settings
     * @see com.google.android.gms.location.SettingsApi
     */
    public Observable<LocationSettingsResult> checkLocationSettings(final LocationSettingsRequest locationRequest) {
        return getGoogleApiClientObservable(LocationServices.API)
                .flatMap(new Function<GoogleApiClient, Observable<LocationSettingsResult>>() {
                    @Override
                    public Observable<LocationSettingsResult> apply(GoogleApiClient googleApiClient) {
                        return fromPendingResult(LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationRequest));
                    }
                });
    }

    /**
     * Returns observable that fetches current place from Places API. To flatmap and auto release
     * buffer to {@link com.google.android.gms.location.places.PlaceLikelihood} observable use
     * {@link DataBufferObservable}.
     *
     * @param placeFilter filter
     * @return observable that emits current places buffer and completes
     */
    public Observable<PlaceLikelihoodBuffer> getCurrentPlace(@Nullable final PlaceFilter placeFilter) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Function<GoogleApiClient, Observable<PlaceLikelihoodBuffer>>() {
                    @Override
                    public Observable<PlaceLikelihoodBuffer> apply(GoogleApiClient api) {
                        return fromPendingResult(Places.PlaceDetectionApi.getCurrentPlace(api, placeFilter));
                    }
                });
    }

    /**
     * Returns observable that fetches a place from the Places API using the place ID.
     *
     * @param placeId id for place
     * @return observable that emits places buffer and completes
     */
    public Observable<PlaceBuffer> getPlaceById(@Nullable final String placeId) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Function<GoogleApiClient, Observable<PlaceBuffer>>() {
                    @Override
                    public Observable<PlaceBuffer> apply(GoogleApiClient api) {
                        return fromPendingResult(Places.GeoDataApi.getPlaceById(api, placeId));
                    }
                });
    }

    /**
     * Returns observable that fetches autocomplete predictions from Places API. To flatmap and autorelease
     * {@link com.google.android.gms.location.places.AutocompletePredictionBuffer} you can use
     * {@link DataBufferObservable}.
     *
     * @param query  search query
     * @param bounds bounds where to fetch suggestions from
     * @param filter filter
     * @return observable with suggestions buffer and completes
     */
    public Observable<AutocompletePredictionBuffer> getPlaceAutocompletePredictions(final String query, final LatLngBounds bounds, final AutocompleteFilter filter) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Function<GoogleApiClient, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> apply(GoogleApiClient api) {
                        return fromPendingResult(Places.GeoDataApi.getAutocompletePredictions(api, query, bounds, filter));
                    }
                });
    }

    /**
     * Returns observable that fetches photo metadata from the Places API using the place ID.
     *
     * @param placeId id for place
     * @return observable that emits metadata buffer and completes
     */
    public Observable<PlacePhotoMetadataResult> getPhotoMetadataById(final String placeId) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Function<GoogleApiClient, Observable<PlacePhotoMetadataResult>>() {
                    @Override
                    public Observable<PlacePhotoMetadataResult> apply(GoogleApiClient api) {
                        return fromPendingResult(Places.GeoDataApi.getPlacePhotos(api, placeId));
                    }
                });
    }

    /**
     * Returns observable that fetches a placePhotoMetadata from the Places API using the place placePhotoMetadata metadata.
     * Use after fetching the place placePhotoMetadata metadata with {@link ReactiveLocationProvider#getPhotoMetadataById(String)}
     *
     * @param placePhotoMetadata the place photo meta data
     * @return observable that emits the photo result and completes
     */
    public Observable<PlacePhotoResult> getPhotoForMetadata(final PlacePhotoMetadata placePhotoMetadata) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Function<GoogleApiClient, Observable<PlacePhotoResult>>() {
                    @Override
                    public Observable<PlacePhotoResult> apply(GoogleApiClient api) {
                        return fromPendingResult(placePhotoMetadata.getPhoto(api));
                    }
                });
    }


    public Observable<GoogleApiClient> getGoogleApiClientObservable(Api... apis) {
        //noinspection unchecked
        return GoogleAPIClientObservableOnSubscribe.create(ctx, factory, apis);
    }

    /**
     * Util method that wraps {@link com.google.android.gms.common.api.PendingResult} in Observable.
     *
     * @param result pending result to wrap
     * @param <T>    parameter type of result
     * @return observable that emits pending result and completes
     */
    public static <T extends Result> Observable<T> fromPendingResult(PendingResult<T> result) {
        return Observable.create(new PendingResultObservableOnSubscribe<>(result));
    }
}
