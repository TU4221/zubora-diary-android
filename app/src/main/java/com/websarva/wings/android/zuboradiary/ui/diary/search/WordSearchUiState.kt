package com.websarva.wings.android.zuboradiary.ui.diary.search

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import kotlinx.parcelize.Parcelize

/**
 * ワード検索画面のUI状態を表すデータクラス。
 *
 * @property searchWord ユーザーによって入力された検索ワード。
 * @property numWordSearchResults 検索にヒットした結果の総数。
 * @property wordSearchResultList 検索結果として表示する日記のリスト。
 *
 * @property isWordSearchIdle 検索が実行されておらず、アイドル状態であるかを示す。
 * @property hasWordSearchCompleted 検索処理が完了したかを示す。
 * @property hasNoWordSearchResults 検索結果が0件であったかを示す。
 *
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 * @property isRefreshing リストの更新中であるかを示す。
 */
@Parcelize
data class WordSearchUiState(
    // UiData
    val searchWord: String = "",
    val numWordSearchResults: Int = 0,
    val wordSearchResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult> = DiaryListUi(),

    // UiState
    val isWordSearchIdle: Boolean = true,
    val hasWordSearchCompleted: Boolean = false,
    val hasNoWordSearchResults: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: WordSearchUiState): WordSearchUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
}
