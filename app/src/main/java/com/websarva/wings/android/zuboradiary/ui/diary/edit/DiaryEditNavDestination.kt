package com.websarva.wings.android.zuboradiary.ui.diary.edit

import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionUi
import java.time.LocalDate

/**
 * 日記編集画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryEditNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: DiaryEditAppMessage) : DiaryEditNavDestination

    /**
     * 日記表示画面。
     * 
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class DiaryShowScreen(val id: String, val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日記項目タイトル編集ダイアログ。
     *
     * @property diaryItemTitleSelection 編集対象のタイトル情報。
     */
    data class DiaryItemTitleEditDialog(
        val diaryItemTitleSelection: DiaryItemTitleSelectionUi
    ) : DiaryEditNavDestination

    /**
     * 既存日記の読み込み確認ダイアログ。
     *
     * @property date 読み込み対象の日付。
     */
    data class DiaryLoadDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日記読み込み失敗ダイアログ。
     *
     * @property date 読み込みに失敗した日記の日付。
     */
    data class DiaryLoadFailureDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日記の上書き保存確認ダイアログ。
     *
     * @property date 上書き対象の日付。
     */
    data class DiaryUpdateDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日記削除確認ダイアログ。
     *
     * @property date 削除対象の日付。
     */
    data class DiaryDeleteDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日付選択ダイアログ。
     *
     * @property date 初期選択する日付。
     */
    data class DatePickerDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 天気情報取得確認ダイアログ。
     *
     * @property date 天気情報を取得する日付。
     */
    data class WeatherInfoFetchDialog(val date: LocalDate) : DiaryEditNavDestination

    /**
     * 日記項目削除確認ダイアログ。
     *
     * @property itemNumber 削除対象の項目番号。
     */
    data class DiaryItemDeleteDialog(val itemNumber: Int) : DiaryEditNavDestination

    /** 添付画像削除確認ダイアログ。 */
    data object DiaryImageDeleteDialog : DiaryEditNavDestination

    /** 未保存終了確認ダイアログ。 */
    data object ExitWithoutDiarySaveDialog : DiaryEditNavDestination
}
