package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class WordSearchUiState(
    val searchWord: String = "",
    val numWordSearchResults: Int = 0,
    val wordSearchResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult> = DiaryYearMonthListUi(),

    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false,
    val isIdle: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasWordSearchCompleted: Boolean = false,
    val hasNoWordSearchResults: Boolean = false,
) : UiState, Parcelable
