package com.websarva.wings.android.zuboradiary.ui.model.event

internal sealed class ViewModelEvent {
    data object NavigatePreviousFragment : ViewModelEvent()
}
