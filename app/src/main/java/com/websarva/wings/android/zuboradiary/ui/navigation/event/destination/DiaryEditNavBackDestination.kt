package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

/**
 * 日記編集画面における画面後方遷移先を表す。
 *
 * 各サブクラスは、後方遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface DiaryEditNavBackDestination : AppNavBackDestination {

    /** 日記の表示・編集フローを終了し、呼び出し元の画面（一覧やカレンダーなど）へ戻る。 */
    data object ExitDiaryFlow : DiaryEditNavBackDestination
}
