package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.common.state.InputTextValidationState
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import kotlinx.parcelize.Parcelize

/**
 * 日記項目タイトル編集ダイアログのUI状態を表すデータクラス。
 *
 * @property itemNumber 編集対象の項目番号。
 * @property title 現在編集中のタイトル文字列。
 * @property titleSelectionHistoriesLoadState 選択履歴リストの読み込み状態。
 *
 * @property titleValidationState タイトル文字列のバリデーション状態。
 *
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
@Parcelize
data class DiaryItemTitleEditUiState(
    // UiData
    val itemNumber: Int = 1,
    val title: String = "",
    val titleSelectionHistoriesLoadState: LoadState<DiaryItemTitleSelectionHistoryListUi> = LoadState.Idle,

    // UiState
    val titleValidationState: InputTextValidationState = InputTextValidationState.Valid,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: DiaryItemTitleEditUiState): DiaryItemTitleEditUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
}
