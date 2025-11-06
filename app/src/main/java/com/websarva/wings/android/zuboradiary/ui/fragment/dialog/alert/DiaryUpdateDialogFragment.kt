package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString

class DiaryUpdateDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_update_title)
    }

    override fun createMessage(): String {
        val updateDiaryDate =
            DiaryUpdateDialogFragmentArgs.fromBundle(requireArguments()).date
        val updateDiaryDateString = updateDiaryDate.formatDateString(requireContext())
        return updateDiaryDateString + getString(R.string.dialog_diary_update_message)
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

    internal companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryUpdateDialogFragment::class.java.name
    }
}
