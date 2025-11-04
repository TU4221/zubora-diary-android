package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent

fun interface MainUiEventHandler<in E : UiEvent> {
    fun onMainUiEventReceived(event: E)
}
