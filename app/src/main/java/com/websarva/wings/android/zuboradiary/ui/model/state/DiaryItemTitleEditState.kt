package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryItemTitleEditState : UiState {
    data object Idle: DiaryItemTitleEditState()
}
