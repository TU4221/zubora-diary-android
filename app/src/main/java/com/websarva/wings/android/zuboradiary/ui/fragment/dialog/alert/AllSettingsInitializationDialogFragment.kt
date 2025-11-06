package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class AllSettingsInitializationDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_all_settings_initialization_title)
    }

    override fun createMessage(): String {
        return getString(R.string.dialog_all_settings_initialization_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + AllSettingsInitializationDialogFragment::class.java.name
    }
}
