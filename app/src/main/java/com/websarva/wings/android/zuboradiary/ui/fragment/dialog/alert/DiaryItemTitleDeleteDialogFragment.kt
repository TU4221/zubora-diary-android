package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class DiaryItemTitleDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryItemTitleDeleteDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_item_title_delete_title)
    }

    override fun createMessage(): String {
        val deleteItemTitle =
            DiaryItemTitleDeleteDialogFragmentArgs.fromBundle(requireArguments()).parameters.itemTitle
        return getString(R.string.dialog_diary_item_title_delete_first_message) + deleteItemTitle + getString(
            R.string.dialog_diary_item_title_delete_second_message
        )
    }

    override fun handleOnPositiveButtonClick() {
        val parameters =
            DiaryItemTitleDeleteDialogFragmentArgs.fromBundle(requireArguments()).parameters
        setResult(KEY_RESULT, DialogResult.Positive(parameters))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
