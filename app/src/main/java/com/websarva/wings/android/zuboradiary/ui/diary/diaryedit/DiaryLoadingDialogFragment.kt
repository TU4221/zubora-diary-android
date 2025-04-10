package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class DiaryLoadingDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryLoadingDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_loading_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryLoadingDialogFragmentArgs.fromBundle(requireArguments()).date
        val converter = DateTimeStringConverter()
        val diaryDateString = converter.toYearMonthDayWeek(diaryDate)
        return diaryDateString + getString(R.string.dialog_diary_loading_message)
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
