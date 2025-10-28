package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
internal data class DiaryListUiState(
    // UiData
    val diaryList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard> = DiaryYearMonthListUi(),
    val sortConditionDate: LocalDate? = null,

    // UiState
    val hasNoDiaries: Boolean = false,

    // ProcessingState
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable
