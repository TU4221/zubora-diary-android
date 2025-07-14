package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DiaryListItemDeleteResult
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString


class DiaryListDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryListDeleteDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_delete_title)
    }

    override fun createMessage(): String {
        val date = DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateString = date.toJapaneseDateString(requireContext())
        return dateString + getString(R.string.dialog_diary_delete_message)
    }

    override fun handleOnPositiveButtonClick() {

        val deleteDiaryDate =
            DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val deleteDiaryImageUri =
            DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).imageUri
        val resultData = DiaryListItemDeleteResult(deleteDiaryDate, deleteDiaryImageUri)
        setResult(KEY_RESULT, DialogResult.Positive(resultData))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
