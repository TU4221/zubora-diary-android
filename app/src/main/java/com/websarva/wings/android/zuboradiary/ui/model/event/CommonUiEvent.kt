package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult

internal sealed class CommonUiEvent : UiEvent {
    data class NavigatePreviousFragment<T>(
        val result: FragmentResult<T> = FragmentResult.None
    ) : CommonUiEvent()
    data class NavigateAppMessage(val message: AppMessage) : CommonUiEvent()
}
