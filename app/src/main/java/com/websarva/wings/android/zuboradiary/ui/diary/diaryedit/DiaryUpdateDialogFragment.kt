package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class DiaryUpdateDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryUpdateDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_update_title)
    }

    override fun createMessage(): String {
        val updateDiaryDate =
            DiaryUpdateDialogFragmentArgs.fromBundle(requireArguments()).date
        val converter = DateTimeStringConverter()
        val updateDiaryDateString = converter.toYearMonthDayWeek(updateDiaryDate)
        return updateDiaryDateString + getString(R.string.dialog_diary_update_message)
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
