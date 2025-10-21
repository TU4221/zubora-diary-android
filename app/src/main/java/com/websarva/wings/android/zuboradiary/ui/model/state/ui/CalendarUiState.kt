package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
internal data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val previousSelectedDate: LocalDate? = null,
    val shouldSmoothScroll: Boolean = false,
    override val diaryLoadState: LoadState<DiaryUi> = LoadState.Idle,
    override val isWeather2Visible: Boolean = false,
    override val numVisibleDiaryItems: Int = 1,
    override val diaryImageFilePath: FilePathUi? = null,
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, DiaryUiState, Parcelable
