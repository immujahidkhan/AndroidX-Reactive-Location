package com.android_reactive_location_sample;

import android.Manifest;

import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions3.RxPermissions;

public abstract class BaseActivity extends AppCompatActivity {

    public RxPermissions rxPermissions;

    @Override
    protected void onStart() {
        super.onStart();
        rxPermissions = new RxPermissions(this); // where this is an Activity or Fragment instance
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        onLocationPermissionGranted();
                    } else {
                        Toast.makeText(BaseActivity.this, "Sorry, no demo without permission...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected abstract void onLocationPermissionGranted();
}