package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryListFragment
import java.time.LocalDate
import java.time.Year

/**
 * 日記一覧画面([DiaryListFragment])における、UIイベントを表すsealed class。
 */
sealed class DiaryListUiEvent : UiEvent {

    /**
     * 日記表示画面へ遷移することを示すイベント。
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryListUiEvent()

    /**
     * 日記編集画面へ遷移することを示すイベント。
     * @property id 編集対象の日記ID。新規作成の場合は`null`。
     * @property date 対象の日記の日付。
     */
    data class NavigateDiaryEditFragment(
        val id: String? = null,
        val date: LocalDate
    ) : DiaryListUiEvent()

    /**
     * 開始年月選択ダイアログへ遷移することを示すイベント。
     * @property maxYear 選択可能な最大の年。
     * @property minYear 選択可能な最小の年。
     */
    data class NavigateStartYearMonthPickerDialog(
        val maxYear: Year,
        val minYear: Year
    ) : DiaryListUiEvent()

    /** ワード検索画面へ遷移することを示すイベント。 */
    data object NavigateWordSearchFragment : DiaryListUiEvent()

    /**
     * 日記削除確認ダイアログへ遷移することを示すイベント。
     * @property date 削除対象の日付。
     */
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryListUiEvent()
}
