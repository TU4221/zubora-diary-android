package com.websarva.wings.android.zuboradiary

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationWorker
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZuboraDiaryApplication : Application(), DefaultLifecycleObserver, Configuration.Provider {

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

        setUpReminderNotificationChannel()
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(logTag, "onStart()")
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(logTag, "onStop()")
        isAppInForeground = false
    }


    /**
     * 通知チャネルを準備する。
     * Android O (API 26) 以降では、通知を表示する前に通知チャネルを作成する必要がある。
     * このメソッドは、リマインダー通知用のチャネルを作成し、設定する。
     */
    private fun setUpReminderNotificationChannel() {
        // MEMO:NotificationChannelはSdk26以降の機能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelName = getString(R.string.reminder_notification_worker_channel_name)
            val channelDescription = getString(R.string.reminder_notification_worker_channel_description)
            val channel =
                NotificationChannel(
                    ReminderNotificationWorker.CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply{
                    description = channelDescription
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    enableVibration(true)
                    setShowBadge(true)
                }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
