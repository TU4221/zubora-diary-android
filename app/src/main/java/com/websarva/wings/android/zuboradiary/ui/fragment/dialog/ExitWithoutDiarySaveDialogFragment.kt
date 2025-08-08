package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class ExitWithoutDiarySaveDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + ExitWithoutDiarySaveDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_exit_without_diary_save_title)
    }

    override fun createMessage(): String {
        return getString(R.string.dialog_exit_without_diary_save_message)
    }

    override fun handleOnPositiveButtonClick() {
        val parameters =
            ExitWithoutDiarySaveDialogFragmentArgs.fromBundle(requireArguments()).parameters
        setResult(KEY_RESULT, DialogResult.Positive(parameters))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
