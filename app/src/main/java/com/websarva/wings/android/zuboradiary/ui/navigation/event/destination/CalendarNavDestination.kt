package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

import com.websarva.wings.android.zuboradiary.ui.model.message.CalendarAppMessage
import java.time.LocalDate

/**
 * カレンダー画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface CalendarNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: CalendarAppMessage) : CalendarNavDestination

    /**
     * 日記編集画面。
     *
     * @property id 編集対象の日記ID。新規作成の場合は null。
     * @property date 対象の日付。
     */
    data class DiaryEditScreen(val id: String?, val date: LocalDate) : CalendarNavDestination
}
