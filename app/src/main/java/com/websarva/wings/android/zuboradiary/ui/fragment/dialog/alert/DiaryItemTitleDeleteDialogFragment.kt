package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class DiaryItemTitleDeleteDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_item_title_delete_title)
    }

    override fun createMessage(): String {
        val deleteItemTitle =
            DiaryItemTitleDeleteDialogFragmentArgs.fromBundle(requireArguments()).itemTitle
        return getString(R.string.dialog_diary_item_title_delete_first_message) + deleteItemTitle + getString(
            R.string.dialog_diary_item_title_delete_second_message
        )
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
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryItemTitleDeleteDialogFragment::class.java.name
    }
}
