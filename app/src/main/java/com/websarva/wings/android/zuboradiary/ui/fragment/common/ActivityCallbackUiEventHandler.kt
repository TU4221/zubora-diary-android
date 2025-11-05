package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent

fun interface ActivityCallbackUiEventHandler {
    fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent)
}
