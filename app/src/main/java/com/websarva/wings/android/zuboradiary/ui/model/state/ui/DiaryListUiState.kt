package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
internal data class DiaryListUiState(
    val diaryList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard> = DiaryYearMonthListUi(),
    val shouldUpdateDiaryList: Boolean = false, // MEMO:画面遷移、回転時の更新フラグ
    val sortConditionDate: LocalDate? = null,
    val isLoadingOnScrolled: Boolean = false,
    val hasNoDiaries: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
