package com.websarva.wings.android.zuboradiary;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Configuration;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class CustomApplication extends Application implements DefaultLifecycleObserver, Configuration.Provider {
    @Inject
    HiltWorkerFactory workerFactory;
    private boolean isAppInForeground;

    @Override
    public void onCreate() {
        Log.d("ApplicationLifeCycle", "onCreate()");
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // ナイトモード無効化(ライトモード常に有効)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("ApplicationLifeCycle", "onStart()");
        isAppInForeground = true;
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("ApplicationLifeCycle", "onStop()");
        isAppInForeground = false;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setWorkerFactory(workerFactory).build();
    }

    // Getter/Setter
    public boolean getIsAppInForeground() {
        return isAppInForeground;
    }
}
