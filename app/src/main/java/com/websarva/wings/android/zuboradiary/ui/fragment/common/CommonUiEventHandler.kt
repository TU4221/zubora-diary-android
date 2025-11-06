package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent

interface CommonUiEventHandler {
    fun onCommonUiEventReceived(event: CommonUiEvent)
}
