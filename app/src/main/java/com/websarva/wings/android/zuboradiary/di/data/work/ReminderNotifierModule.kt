package com.websarva.wings.android.zuboradiary.di.data.work

import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * [ReminderNotifier]の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * インターフェースは、対応する実装クラスと `@Binds` アノテーションによって紐付けられる。
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReminderNotifierModule {

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Binds
    abstract fun bindReminderNotifier(
        impl: ReminderNotifierImpl
    ): ReminderNotifier
}
