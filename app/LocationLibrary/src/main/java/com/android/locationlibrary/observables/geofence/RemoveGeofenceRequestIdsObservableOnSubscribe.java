package com.android.locationlibrary.observables.geofence;


import androidx.annotation.NonNull;

import com.android.locationlibrary.observables.ObservableContext;
import com.android.locationlibrary.observables.StatusException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import io.reactivex.ObservableEmitter;


class RemoveGeofenceRequestIdsObservableOnSubscribe extends RemoveGeofenceObservableOnSubscribe<Status> {
    private final List<String> geofenceRequestIds;

    RemoveGeofenceRequestIdsObservableOnSubscribe(ObservableContext ctx, List<String> geofenceRequestIds) {
        super(ctx);
        this.geofenceRequestIds = geofenceRequestIds;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient, final ObservableEmitter<? super Status> emitter) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, geofenceRequestIds)
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
