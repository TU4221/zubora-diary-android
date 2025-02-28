package com.websarva.wings.android.zuboradiary.ui.settings

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

class AllDataDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val FROM_CLASS_NAME = "From" + AllDataDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$FROM_CLASS_NAME"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_all_data_delete_title)
    }

    override fun createMessage(): String {
        return getString(R.string.dialog_all_data_delete_message)
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
