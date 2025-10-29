package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class WordSearchUiState(
    // UiData
    val searchWord: String = "",
    val numWordSearchResults: Int = 0,
    val wordSearchResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult> = DiaryYearMonthListUi(),

    // UiState
    val isIdle: Boolean = true,
    val hasWordSearchCompleted: Boolean = false,
    val hasNoWordSearchResults: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable
