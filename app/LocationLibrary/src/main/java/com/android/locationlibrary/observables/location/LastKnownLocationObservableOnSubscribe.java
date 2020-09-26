package com.android.locationlibrary.observables.location;

import android.location.Location;

import com.android.locationlibrary.observables.BaseLocationObservableOnSubscribe;
import com.android.locationlibrary.observables.ObservableContext;
import com.android.locationlibrary.observables.ObservableFactory;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

@SuppressWarnings("MissingPermission")
public class LastKnownLocationObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Location> {
    public static Observable<Location> createObservable(ObservableContext ctx, ObservableFactory factory) {
        return factory.createObservable(new LastKnownLocationObservableOnSubscribe(ctx));
    }

    private LastKnownLocationObservableOnSubscribe(ObservableContext ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<? super Location> emitter) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (emitter.isDisposed()) return;
        if (location != null) {
            emitter.onNext(location);
        }
        emitter.onComplete();
    }
}
