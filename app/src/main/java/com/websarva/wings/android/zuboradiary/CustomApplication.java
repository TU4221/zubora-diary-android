package com.websarva.wings.android.zuboradiary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class CustomApplication extends Application implements DefaultLifecycleObserver {
    private boolean isAppInForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        isAppInForeground = true;
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        isAppInForeground = false;
    }

    public boolean getIsAppInForeground() {
        return isAppInForeground;
    }
}
