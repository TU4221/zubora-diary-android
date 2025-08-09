package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

    override fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        super.customizeDialog(builder)
        builder.setNegativeButton("", null)
    }
}
