package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.AppMessage

internal sealed class ViewModelEvent {
    data object NavigatePreviousFragment : ViewModelEvent()
    data class NavigateAppMessage(val message: AppMessage) : ViewModelEvent()
}
