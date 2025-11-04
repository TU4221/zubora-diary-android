package com.websarva.wings.android.zuboradiary.ui.model.event

sealed class ActivityCallbackUiEvent : UiEvent {
    data object ProcessOnBottomNavigationItemReselect : ActivityCallbackUiEvent()
}
