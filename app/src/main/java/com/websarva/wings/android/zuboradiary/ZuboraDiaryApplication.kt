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
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.squareup.leakcanary.core.BuildConfig
import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.ui.notification.ReminderNotificationManager
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZuboraDiaryApplication :
    Application(),
    DefaultLifecycleObserver,
    Configuration.Provider,
    SingletonImageLoader.Factory {

    private val logTag = createLogTag()

    @Inject
    lateinit var imageFileDataSource: ImageFileDataSource

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
        try {
            imageFileDataSource.deleteAllFilesInCache()
        } catch (e: Exception) {
            Log.w(logTag, "キャッシュストレージの画像ファイルのクリア失敗", e)
        }

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
                    ReminderNotificationManager.CHANNEL_ID,
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

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache { // メモリキャッシュ設定
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.20) // 利用可能なアプリメモリからメモリキャッシュへの使用料
                    .build()
            }
            .diskCache(null) // ローカルファイルの読み込みの為、ディスクキャッシュを無効化

            // MEMO:画像切替時の表示不具合対策としてクロスフェードを無効化。
            //      有効の場合、アプリリソース画像とストレージ画像間の高速な切り替え時に、以下の問題が発生することがある。
            //      1. 画像が薄く表示される (アルファ値の問題の可能性)
            //      2. ImageView で指定した ScaleType が適用されない
            .crossfade(false) // 画像表示時のクロスフェード有無

            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger()) // ロギング (開発中のみ)
                }
            }
            .build()
    }
}
