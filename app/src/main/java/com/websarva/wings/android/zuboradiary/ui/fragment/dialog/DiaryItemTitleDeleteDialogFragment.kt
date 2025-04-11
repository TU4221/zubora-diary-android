package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R

class DiaryItemTitleDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryItemTitleDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
        @JvmField
        val KEY_DELETE_LIST_ITEM_POSITION: String = "DeleteItemPosition$fromClassName"
    }

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
        setResults(DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick() {
        setResults(DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel() {
        setResults(DialogInterface.BUTTON_NEGATIVE)
    }

    private fun setResults(status: Int) {
        setResult(KEY_SELECTED_BUTTON, status)

        val deleteListItemPosition =
            DiaryItemTitleDeleteDialogFragmentArgs.fromBundle(requireArguments()).itemPosition
        setResult(KEY_DELETE_LIST_ITEM_POSITION, deleteListItemPosition)
    }
}
