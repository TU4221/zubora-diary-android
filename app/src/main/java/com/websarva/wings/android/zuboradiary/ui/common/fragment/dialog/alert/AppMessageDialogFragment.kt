package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.alert

import androidx.navigation.fragment.navArgs
import kotlin.getValue

/**
 * アプリケーション内の様々なメッセージ（エラー、情報など）をユーザーに通知するための汎用的な警告ダイアログ。
 */
class AppMessageDialogFragment() : BaseAlertDialogFragment() {

    /** 画面遷移時に渡された引数。 */
    private val navArgs: AppMessageDialogFragmentArgs by navArgs()

    override val title get() = navArgs.appMessage.getDialogTitle(requireContext())

    override val message get() = navArgs.appMessage.getDialogMessage(requireContext())

    override val negativeButtonText = null

    override fun handleOnCancel() {
        // 処理なし
    }
}
