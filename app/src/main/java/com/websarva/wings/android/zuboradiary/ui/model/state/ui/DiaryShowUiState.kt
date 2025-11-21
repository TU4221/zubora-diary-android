package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryShowFragment
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import kotlinx.parcelize.Parcelize

/**
 * 日記表示画面([DiaryShowFragment])のUI状態を表すデータクラス。
 *
 * @property diaryLoadState 日記データの読み込み状態。
 * @property diaryImageFilePath 添付画像のファイルパス。
 * 
 * @property isWeather2Visible 天気2が表示されるべきかを示す。
 * @property numVisibleDiaryItems 表示されている日記項目の数。
 * 
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
@Parcelize
data class DiaryShowUiState(
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
