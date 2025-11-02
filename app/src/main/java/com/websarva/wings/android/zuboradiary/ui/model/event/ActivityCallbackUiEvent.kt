package com.websarva.wings.android.zuboradiary.ui.model.event

internal sealed class ActivityCallbackUiEvent : UiEvent {
    data object ProcessOnBottomNavigationItemReselect : ActivityCallbackUiEvent()
}
