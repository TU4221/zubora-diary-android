package com.websarva.wings.android.zuboradiary.ui.settings

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class AllSettingsInitializationDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val FROM_CLASS_NAME =
            "From" + AllSettingsInitializationDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$FROM_CLASS_NAME"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_all_settings_initialization_title)
    }

    override fun createMessage(): String {
        return getString(R.string.dialog_all_settings_initialization_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }
}
