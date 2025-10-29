package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState

internal interface BaseDiaryShowUiState {
    val diaryLoadState: LoadState<DiaryUi>
    val isWeather2Visible: Boolean
    val numVisibleDiaryItems: Int
    val diaryImageFilePath: FilePathUi?
}
