package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日記編集画面のUI状態を表すデータクラス。
 *
 * @property originalDiaryLoadState 編集開始時の元の日記データの読み込み状態。
 * @property editingDiary 現在編集中の日記データ。
 * @property previousSelectedDate 以前（一つ前）に選択されていた日付。
 * @property weather1Options 天気1の選択肢リスト。
 * @property weather2Options 天気2の選択肢リスト。
 * @property conditionOptions 体調の選択肢リスト。
 * @property diaryImageFilePath 添付画像のファイルパス。
 * @property diaryItemTitleSelectionHistories 日記項目タイトルの選択（編集）履歴。
 *
 * @property isNewDiary 新規作成中の日記であるかを示す。
 * @property isWeather2Enabled 天気2が有効かを示す。
 * @property numVisibleDiaryItems 表示されている日記項目の数。
 * @property isDiaryItemAdditionEnabled 日記項目を追加可能かを示す。
 * 
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
@Parcelize
data class DiaryEditUiState(
    // UiData
    val originalDiaryLoadState: LoadState<DiaryUi> = LoadState.Idle,
    val editingDiary: DiaryUi,
    val previousSelectedDate: LocalDate? = null,
    val weather1Options: List<WeatherUi> = WeatherUi.entries,
    val weather2Options: List<WeatherUi> = WeatherUi.entries,
    val conditionOptions: List<ConditionUi> = ConditionUi.entries,
    val diaryImageFilePath: FilePathUi? = null,
    val diaryItemTitleSelectionHistories: Map<Int, DiaryItemTitleSelectionHistoryUi?> = emptyMap(),

    // UiState
    val isNewDiary: Boolean = false,
    val isWeather2Enabled: Boolean = false,
    val numVisibleDiaryItems: Int = 1,
    val isDiaryItemAdditionEnabled: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: DiaryEditUiState): DiaryEditUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
}
