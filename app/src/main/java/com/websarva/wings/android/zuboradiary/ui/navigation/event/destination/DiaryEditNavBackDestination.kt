package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

/**
 * 日記編集画面における画面後方遷移先を表す。
 *
 * 各サブクラスは、後方遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryEditNavBackDestination : AppNavBackDestination {

    /**
     * ボトムナビゲーションの現在選択されているタブに割り当てられた画面。
     */
    data object SelectedTabScreen : DiaryEditNavBackDestination
}
