package com.websarva.wings.android.zuboradiary

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class CustomApplication : Application(), DefaultLifecycleObserver, Configuration.Provider {

    private val logTag = createLogTag()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    var isAppInForeground = false
        private set
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        Log.d(logTag, "onCreate()")
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // ナイトモード無効化(ライトモード常に有効)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(logTag, "onStart()")
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(logTag, "onStop()")
        isAppInForeground = false
    }
}
