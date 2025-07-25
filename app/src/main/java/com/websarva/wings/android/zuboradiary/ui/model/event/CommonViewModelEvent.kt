package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.AppMessage

internal sealed class CommonViewModelEvent : ViewModelEvent {
    data object NavigatePreviousFragment : CommonViewModelEvent()
    data class NavigateAppMessage(val message: AppMessage) : CommonViewModelEvent()
}
