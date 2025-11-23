package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.AppMessageDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage

/**
 * アプリケーション全体で共通のUIイベントを処理するためのインターフェース。
 *
 * 画面遷移や共通のメッセージ表示など、複数の画面で共有されるUIイベントの処理を定義する。
 */
interface CommonUiEventHandler {

    /**
     * 前の画面へ遷移する。
     */
    fun navigatePreviousFragment()

    /**
     * アプリケーションメッセージダイアログ([AppMessageDialogFragment])へ遷移する。
     * @param appMessage 表示するメッセージ情報
     */
    fun navigateAppMessageDialog(appMessage: AppMessage)
}
