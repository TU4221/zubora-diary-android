package com.websarva.wings.android.zuboradiary.di.data.common

import com.websarva.wings.android.zuboradiary.ZuboraDiaryApplication
import com.websarva.wings.android.zuboradiary.data.common.AppForegroundStateProvider

internal class AppForegroundStateProviderImpl(
    private val application: ZuboraDiaryApplication
): AppForegroundStateProvider {

    override val isAppInForeground
        get() = application.isAppInForeground
}
