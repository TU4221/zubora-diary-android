package com.websarva.wings.android.zuboradiary.ui.main

import com.websarva.wings.android.zuboradiary.ui.common.activity.MainActivityAppMessage
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination

/**
 * メインアクティビティにおける画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface MainActivityNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: MainActivityAppMessage) : MainActivityNavDestination
}
