package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.result.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class DiaryItemTitleEditUiState(
    // UiData
    val itemNumber: Int = 1,
    val title: String = "",
    val titleSelectionHistoriesLoadState: LoadState<DiaryItemTitleSelectionHistoryListUi> = LoadState.Idle,

    // UiState
    val titleValidationResult: InputTextValidationResult = InputTextValidationResult.Valid,

    // ProcessingState
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
