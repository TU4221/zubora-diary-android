package com.websarva.wings.android.zuboradiary.ui.model.event

internal sealed class FragmentUiEvent : UiEvent {
    data object ProcessOnBottomNavigationItemReselect : FragmentUiEvent()
}
