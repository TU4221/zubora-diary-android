package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.InputTextValidationState
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryItemTitleEditUiState(
    // UiData
    val itemNumber: Int = 1,
    val title: String = "",
    val titleSelectionHistoriesLoadState: LoadState<DiaryItemTitleSelectionHistoryListUi> = LoadState.Idle,

    // UiState
    val titleValidationState: InputTextValidationState = InputTextValidationState.Valid,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, Parcelable
