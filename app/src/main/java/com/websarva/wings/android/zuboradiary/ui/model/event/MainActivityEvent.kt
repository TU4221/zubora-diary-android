package com.websarva.wings.android.zuboradiary.ui.model.event

internal sealed class MainActivityEvent : UiEvent {
    data object NavigateStartTabFragment : MainActivityEvent()
}
