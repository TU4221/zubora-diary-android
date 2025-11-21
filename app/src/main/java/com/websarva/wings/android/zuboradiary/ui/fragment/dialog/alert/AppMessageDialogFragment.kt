package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * アプリケーション内の様々なメッセージ（エラー、情報など）をユーザーに通知するための汎用的な警告ダイアログ。
 */
class AppMessageDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        val appMessage = AppMessageDialogFragmentArgs.fromBundle(requireArguments()).appMessage
        return appMessage.getDialogTitle(requireContext())
    }

    override fun createMessage(): String {
        val appMessage = AppMessageDialogFragmentArgs.fromBundle(requireArguments()).appMessage
        return appMessage.getDialogMessage(requireContext())
    }

    override fun handleOnPositiveButtonClick() {
        // 処理なし
    }

    override fun handleOnNegativeButtonClick() {
        // 処理なし
    }

    override fun handleOnCancel() {
        // 処理なし
    }

    /** 追加処理として、Negativeボタンを無効化。 */
    override fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        super.customizeDialog(builder)
        builder.setNegativeButton("", null)
    }
}
