package com.android.locationlibrary.observables.activity;

import com.android.locationlibrary.observables.BaseObservableOnSubscribe;
import com.android.locationlibrary.observables.ObservableContext;
import com.google.android.gms.location.ActivityRecognition;



abstract class BaseActivityObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    BaseActivityObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, ActivityRecognition.API);
    }
}
