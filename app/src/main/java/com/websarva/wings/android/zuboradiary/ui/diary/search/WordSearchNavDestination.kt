package com.websarva.wings.android.zuboradiary.ui.diary.search

import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination
import java.time.LocalDate

/**
 * ワード検索画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface WordSearchNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: WordSearchAppMessage) : WordSearchNavDestination

    /**
     * 日記表示画面。
     *
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class DiaryShowScreen(val id: String, val date: LocalDate) : WordSearchNavDestination
}
