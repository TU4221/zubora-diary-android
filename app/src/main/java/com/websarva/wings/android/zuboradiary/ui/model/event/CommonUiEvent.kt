package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.AppMessage

internal sealed class CommonUiEvent : UiEvent {
    data object NavigatePreviousFragment : CommonUiEvent()
    data class NavigateAppMessage(val message: AppMessage) : CommonUiEvent()
}
