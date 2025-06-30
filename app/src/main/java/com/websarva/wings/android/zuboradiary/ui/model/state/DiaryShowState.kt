package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryShowState : ViewModelState {
    data object Idle: DiaryShowState() // 初期状態

    data object Loading : DiaryShowState()
    data object LoadSuccess : DiaryShowState()
    data object LoadError : DiaryShowState()

    data object Deleting : DiaryShowState()
}
