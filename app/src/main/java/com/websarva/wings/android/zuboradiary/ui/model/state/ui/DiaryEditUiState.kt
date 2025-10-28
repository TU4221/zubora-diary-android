package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

// TODO:初期化はstaticメソッドでやる
@Parcelize
internal data class DiaryEditUiState(
    val isNewDiary: Boolean = false,
    val originalDiaryLoadState: LoadState<DiaryUi> = LoadState.Idle,
    val editingDiary: DiaryUi,
    val previousSelectedDate: LocalDate? = null,
    val weather1Options: List<WeatherUi> = WeatherUi.entries,
    val weather2Options: List<WeatherUi> = WeatherUi.entries,
    val isWeather2Enabled: Boolean = false,
    val conditionOptions: List<ConditionUi> = ConditionUi.entries,
    val numVisibleDiaryItems: Int = 1,
    val isDiaryItemAdditionEnabled: Boolean = false,
    val diaryImageFilePath: FilePathUi? = null,
    val diaryItemTitleSelectionHistories: Map<Int, DiaryItemTitleSelectionHistoryUi?> = emptyMap(),

    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
