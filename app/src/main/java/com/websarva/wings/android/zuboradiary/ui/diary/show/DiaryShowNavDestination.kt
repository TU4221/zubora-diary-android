package com.websarva.wings.android.zuboradiary.ui.diary.show

import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination
import java.time.LocalDate

/**
 * 日記表示画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryShowNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: DiaryShowAppMessage) : DiaryShowNavDestination

    /**
     * 日記編集画面。
     *
     * @property id 編集対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class DiaryEditScreen(val id: String, val date: LocalDate) : DiaryShowNavDestination

    /**
     * 日記読み込み失敗ダイアログ。
     *
     * @property date 読み込みに失敗した日記の日付。
     */
    data class DiaryLoadFailureDialog(val date: LocalDate) : DiaryShowNavDestination

    /**
     * 日記削除確認ダイアログ。
     *
     * @property date 削除対象の日付。
     */
    data class DiaryDeleteDialog(val date: LocalDate) : DiaryShowNavDestination
}
