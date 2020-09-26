package com.android_reactive_location_sample.utils;

import android.widget.TextView;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import io.reactivex.Observable;

public class RxTextView {
    @CheckResult
    @NonNull
    public static Observable<CharSequence> textChanges(@NonNull TextView view) {
        return Observable.create(new TextViewTextOnSubscribe(view));
    }
}
