package com.websarva.wings.android.zuboradiary.ui.diary.list

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日記一覧画面のUI状態を表すデータクラス。
 *
 * @property diaryList 表示する日記のリスト。
 * @property sortConditionDate 日記リストのソート条件日付。
 *
 * @property hasNoDiaries 保存された、又はソート条件にあった日記がなかったかを示す。
 *
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 * @property isRefreshing リストの更新中であるかを示す。
 */
@Parcelize
data class DiaryListUiState(
    // UiData
    val diaryList: DiaryListUi<DiaryListItemContainerUi.Standard> = DiaryListUi(),
    val sortConditionDate: LocalDate? = null,

    // UiState
    val hasNoDiaries: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: DiaryListUiState): DiaryListUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
}
