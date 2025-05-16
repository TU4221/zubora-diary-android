package com.websarva.wings.android.zuboradiary.ui.model.state

internal sealed class DiaryShowState {
    data object Idle : DiaryShowState()
    data object Loading : DiaryShowState() // 読込中
    data object Deleting : DiaryShowState() // 削除中
}
