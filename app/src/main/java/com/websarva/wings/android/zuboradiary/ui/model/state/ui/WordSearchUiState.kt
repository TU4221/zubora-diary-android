package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordSearchUiState(
    // UiData
    val searchWord: String = "",
    val numWordSearchResults: Int = 0,
    val wordSearchResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult> = DiaryListUi(),

    // UiState
    val isWordSearchIdle: Boolean = true,
    val hasWordSearchCompleted: Boolean = false,
    val hasNoWordSearchResults: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: WordSearchUiState): WordSearchUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
}
