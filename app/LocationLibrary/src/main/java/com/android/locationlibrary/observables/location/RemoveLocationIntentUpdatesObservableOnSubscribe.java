package com.android.locationlibrary.observables.location;

import android.app.PendingIntent;

import androidx.annotation.NonNull;

import com.android.locationlibrary.observables.BaseLocationObservableOnSubscribe;
import com.android.locationlibrary.observables.ObservableContext;
import com.android.locationlibrary.observables.ObservableFactory;
import com.android.locationlibrary.observables.StatusException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;


public class RemoveLocationIntentUpdatesObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Status> {
    private final PendingIntent intent;

    public static Observable<Status> createObservable(ObservableContext ctx, ObservableFactory factory, PendingIntent intent) {
        return factory.createObservable(new RemoveLocationIntentUpdatesObservableOnSubscribe(ctx, intent));
    }

    private RemoveLocationIntentUpdatesObservableOnSubscribe(ObservableContext ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<? super Status> emitter) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (emitter.isDisposed()) return;
                        if (status.isSuccess()) {
                            emitter.onNext(status);
                            emitter.onComplete();
                        } else {
                            emitter.onError(new StatusException(status));
                        }
                    }
                });
    }
}
