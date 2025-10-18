package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryShowState : UiState, Parcelable {
    data object Idle: DiaryShowState() // 初期状態

    data object Loading : DiaryShowState()
    data object LoadSuccess : DiaryShowState()
    data object LoadError : DiaryShowState()

    data object Deleting : DiaryShowState()
}
