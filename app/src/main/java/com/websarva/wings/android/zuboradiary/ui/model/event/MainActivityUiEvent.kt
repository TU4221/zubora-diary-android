package com.websarva.wings.android.zuboradiary.ui.model.event

internal sealed class MainActivityUiEvent : UiEvent {
    data object NavigateStartTabFragment : MainActivityUiEvent()
}
