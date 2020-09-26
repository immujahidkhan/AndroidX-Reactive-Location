package com.android.locationlibrary.observables;

import android.content.Context;
import android.os.Handler;

import com.android.locationlibrary.ReactiveLocationProviderConfiguration;


public class ObservableContext {
    private final Context context;
    private final Handler handler;
    private final boolean retryOnConnectionSuspended;

    public ObservableContext(Context context, ReactiveLocationProviderConfiguration configuration) {
        this.context = context;
        this.handler = configuration.getCustomCallbackHandler();
        this.retryOnConnectionSuspended = configuration.isRetryOnConnectionSuspended();
    }

    public Context getContext() {
        return context;
    }

    Handler getHandler() {
        return handler;
    }

    boolean isRetryOnConnectionSuspended() {
        return retryOnConnectionSuspended;
    }
}
