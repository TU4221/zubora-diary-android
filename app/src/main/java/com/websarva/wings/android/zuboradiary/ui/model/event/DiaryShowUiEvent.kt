package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryShowFragment
import java.time.LocalDate

/**
 * 日記表示画面([DiaryShowFragment])における、UIイベントを表すsealed class。
 */
sealed class DiaryShowUiEvent : UiEvent {

    /**
     * 日記編集画面へ遷移することを示すイベント。
     * @property id 編集対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class NavigateDiaryEditScreen(val id: String, val date: LocalDate) : DiaryShowUiEvent()

    /**
     * 前の画面へデータを渡し、遷移することを示すイベント。
     * @property date 遷移元へ渡す日記の日付。
     */
    data class NavigatePreviousScreenWithResult(val date: LocalDate) : DiaryShowUiEvent()

    /**
     * 日記削除後に前の画面へ遷移することを示すイベント。
     * @property date 削除された日記の日付。
     */
    data class NavigatePreviousScreenOnDiaryDeleted(val date: LocalDate) : DiaryShowUiEvent()

    /** 日記の初期読み込み失敗後に前の画面へ遷移することを示すイベント。 */
    data object NavigatePreviousScreenOnDiaryLoadFailed : DiaryShowUiEvent()

    /**
     * 日記読み込み失敗ダイアログを表示することを示すイベント。
     * @property date 読み込みに失敗した日記の日付。
     */
    data class ShowDiaryLoadFailureDialog(val date: LocalDate) : DiaryShowUiEvent()

    /**
     * 日記削除確認ダイアログを表示することを示すイベント。
     * @property date 削除対象の日付。
     */
    data class ShowDiaryDeleteDialog(val date: LocalDate) : DiaryShowUiEvent()
}
