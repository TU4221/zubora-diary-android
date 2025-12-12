package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryListAppMessage
import java.time.LocalDate
import java.time.Year

/**
 * 日記一覧画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryListNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: DiaryListAppMessage) : DiaryListNavDestination

    /**
     * 日記表示画面。
     *
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class DiaryShowScreen(val id: String, val date: LocalDate) : DiaryListNavDestination

    /**
     * 日記編集画面。
     *
     * @property id 編集対象の日記ID。新規作成の場合は`null`。
     * @property date 対象の日記の日付。
     */
    data class DiaryEditScreen(
        val id: String? = null,
        val date: LocalDate
    ) : DiaryListNavDestination

    /** ワード検索画面。 */
    data object WordSearchScreen : DiaryListNavDestination

    /**
     * 開始年月選択ダイアログ。
     *
     * @property maxYear 選択可能な最大の年。
     * @property minYear 選択可能な最小の年。
     */
    data class StartYearMonthPickerDialog(
        val maxYear: Year,
        val minYear: Year
    ) : DiaryListNavDestination

    /**
     * 日記削除確認ダイアログ。
     *
     * @property date 削除対象の日付。
     */
    data class DiaryDeleteDialog(val date: LocalDate) : DiaryListNavDestination
}
