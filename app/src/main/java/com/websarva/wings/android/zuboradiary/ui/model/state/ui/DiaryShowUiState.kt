package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class DiaryShowUiState(
    // UiData
    override val diaryLoadState: LoadState<DiaryUi> = LoadState.Idle,
    override val diaryImageFilePath: FilePathUi? = null,

    // UiState
    override val isWeather2Visible: Boolean = false,
    override val numVisibleDiaryItems: Int = 1,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, BaseDiaryShowUiState, Parcelable
