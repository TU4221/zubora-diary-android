package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class DiaryItemDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryItemDeleteDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_item_delete_title)
    }

    override fun createMessage(): String {
        val deleteItemNumber =
            DiaryItemDeleteDialogFragmentArgs.fromBundle(requireArguments()).itemNumber
        return getString(R.string.dialog_diary_item_delete_first_message) + deleteItemNumber + getString(
            R.string.dialog_diary_item_delete_second_message
        )
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_RESULT, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
