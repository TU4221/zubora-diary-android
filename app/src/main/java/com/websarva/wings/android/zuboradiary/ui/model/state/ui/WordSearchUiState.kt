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
    val shouldRefreshWordSearchResultList: Boolean = false, // MEMO:画面遷移、回転時の更新フラグ
    val isLoadingOnScrolled: Boolean = false,
    val hasNoWordSearchResults: Boolean = false,
    val isTopScrollFabEnabled: Boolean = false,
    val isNumWordSearchResultsVisible: Boolean = false,
    val isWordSearchResultListVisible: Boolean = false,
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
