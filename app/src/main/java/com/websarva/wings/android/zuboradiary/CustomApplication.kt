package com.websarva.wings.android.zuboradiary

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class CustomApplication : Application(), DefaultLifecycleObserver, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    var isAppInForeground = false
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        Log.d("ApplicationLifeCycle", "onCreate()")
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // ナイトモード無効化(ライトモード常に有効)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("ApplicationLifeCycle", "onStart()")
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("ApplicationLifeCycle", "onStop()")
        isAppInForeground = false
    }
}
