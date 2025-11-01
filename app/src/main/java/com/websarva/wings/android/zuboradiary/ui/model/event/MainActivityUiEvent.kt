package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage

internal sealed class MainActivityUiEvent : UiEvent {
    data class NavigateAppMessage(val message: AppMessage) : MainActivityUiEvent()
    data object NavigateStartTabFragment : MainActivityUiEvent()
}
