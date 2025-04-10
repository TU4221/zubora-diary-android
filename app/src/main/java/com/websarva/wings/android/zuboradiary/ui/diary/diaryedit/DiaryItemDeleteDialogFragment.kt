package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class DiaryItemDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryItemDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_DELETE_ITEM_NUMBER: String = "DeleteItemNumber$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_item_delete_title)
    }

    override fun createMessage(): String {
        val deleteItemNumber =
            DiaryItemDeleteDialogFragmentArgs
                .fromBundle(requireArguments()).itemNumber
        return getString(R.string.dialog_diary_item_delete_first_message) + deleteItemNumber + getString(
            R.string.dialog_diary_item_delete_second_message
        )
    }

    override fun handleOnPositiveButtonClick() {
        val deleteItemNumber =
            DiaryItemDeleteDialogFragmentArgs
                .fromBundle(requireArguments()).itemNumber
        setResult(KEY_DELETE_ITEM_NUMBER, deleteItemNumber)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_DELETE_ITEM_NUMBER, null)
    }

    override fun handleOnCancel() {
        setResult(KEY_DELETE_ITEM_NUMBER, null)
    }
}
