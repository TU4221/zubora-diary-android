package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class DiaryListUiState(
    // UiData
    val diaryList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard> = DiaryYearMonthListUi(),
    val sortConditionDate: LocalDate? = null,

    // UiState
    val hasNoDiaries: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: DiaryListUiState): DiaryListUiState {
            return DiaryListUiState(
                diaryList = savedUiState.diaryList,
                sortConditionDate = savedUiState.sortConditionDate
            )
        }
    }
}
