package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class DiaryDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_delete_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val diaryDateString = DateTimeStringConverter().toYearMonthDayWeek(diaryDate)
        return diaryDateString + getString(R.string.dialog_diary_delete_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }
}
