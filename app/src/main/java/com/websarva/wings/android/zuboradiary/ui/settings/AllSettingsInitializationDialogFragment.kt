package com.websarva.wings.android.zuboradiary.ui.settings

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

class AllSettingsInitializationDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val FROM_CLASS_NAME =
            "From" + AllSettingsInitializationDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$FROM_CLASS_NAME"
    }

    override val isCancelableOtherThanPressingButton: Boolean
        get() = true

    override fun createTitle(): String {
        return getString(R.string.dialog_all_settings_initialization_title)
    }

    override fun createMessage(): String {
        return getString(R.string.dialog_all_settings_initialization_message)
    }

    override fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
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
}
