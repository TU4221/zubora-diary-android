package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

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

    override fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int) {
        val deleteItemNumber =
            DiaryItemDeleteDialogFragmentArgs
                .fromBundle(requireArguments()).itemNumber
        setResult(KEY_DELETE_ITEM_NUMBER, deleteItemNumber)
    }

    override fun handleOnNegativeButtonClick(dialog: DialogInterface, which: Int) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss() {
        // 処理なし
    }
}
