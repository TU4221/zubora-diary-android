package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

class DiaryDeleteDialogFragment : BaseAlertDialogFragment() {

    override val isCancelableOtherThanPressingButton: Boolean
        get() = true

    companion object {
        private val fromClassName = "From" + DiaryDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_delete_title)
    }

    override fun createMessage(): String {
        val date = DiaryDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateTimeStringConverter = DateTimeStringConverter()
        val strDate = dateTimeStringConverter.toYearMonthDayWeek(date)
        return strDate + getString(R.string.dialog_diary_delete_message)
    }

    override fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
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
