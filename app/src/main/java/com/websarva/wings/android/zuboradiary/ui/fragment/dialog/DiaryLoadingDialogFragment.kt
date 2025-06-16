package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString

class DiaryLoadingDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryLoadingDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_loading_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryLoadingDialogFragmentArgs.fromBundle(requireArguments()).parameters.date
        val diaryDateString = diaryDate.toJapaneseDateString(requireContext())
        return diaryDateString + getString(R.string.dialog_diary_loading_message)
    }

    override fun handleOnPositiveButtonClick() {
        val parameters =
            DiaryLoadingDialogFragmentArgs.fromBundle(requireArguments()).parameters
        setResult(KEY_RESULT, DialogResult.Positive(parameters))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
