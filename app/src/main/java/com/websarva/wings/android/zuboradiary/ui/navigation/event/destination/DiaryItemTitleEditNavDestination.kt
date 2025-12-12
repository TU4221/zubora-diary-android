package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryItemTitleEditAppMessage

/**
 * 日記項目タイトル編集ダイアログにおける画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryItemTitleEditNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(
        val message: DiaryItemTitleEditAppMessage
    ) : DiaryItemTitleEditNavDestination

    /**
     * 選択履歴削除確認ダイアログ。
     *
     * @property itemTitle 削除対象の項目タイトル。
     */
    data class SelectionHistoryDeleteDialog(
        val itemTitle: String
    ) : DiaryItemTitleEditNavDestination
}
