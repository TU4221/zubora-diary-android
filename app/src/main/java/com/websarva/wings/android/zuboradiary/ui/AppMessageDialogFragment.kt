package com.websarva.wings.android.zuboradiary.ui

import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AppMessageDialogFragment : BaseAlertDialogFragment() {

    override val isCancelableOtherThanPressingButton
        get() = true

    override fun createTitle(): String {
        val appMessage = AppMessageDialogFragmentArgs.fromBundle(requireArguments()).appMessage
        return appMessage.getDialogTitle(requireContext())
    }

    override fun createMessage(): String {
        val appMessage = AppMessageDialogFragmentArgs.fromBundle(requireArguments()).appMessage
        return appMessage.getDialogMessage(requireContext())
    }

    override fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int) {
        // 処理なし
    }

    override fun handleOnNegativeButtonClick(dialog: DialogInterface, which: Int) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss() {
        // 処理なし
    }

    override fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        super.customizeDialog(builder)
        builder.setNegativeButton("", null)
    }
}
