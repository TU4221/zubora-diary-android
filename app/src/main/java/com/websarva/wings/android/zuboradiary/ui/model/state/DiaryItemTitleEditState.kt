package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryItemTitleEditState : UiState, Parcelable {
    data object Idle: DiaryItemTitleEditState()

    data object LoadingSelectionHistory: DiaryItemTitleEditState()

    data object ShowingSelectionHistory: DiaryItemTitleEditState()
    data object NoSelectionHistory: DiaryItemTitleEditState()
}
