package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.websarva.wings.android.zuboradiary.ui.BaseDatePickerDialogFragment
import java.time.LocalDate


class DatePickerDialogFragment : BaseDatePickerDialogFragment() {

    companion object {
        private val fromClassName = "From" + DatePickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_DATE: String = "SelectedDate$fromClassName"
    }

    override fun createInitialDate(): LocalDate {
        return DatePickerDialogFragmentArgs.fromBundle(requireArguments()).date
    }

    override fun handleOnPositiveButtonClick(selectedDate: LocalDate) {
        Log.d("20250228", "handleOnPositiveButtonClick")
        setSelectedDate(selectedDate)
    }

    override fun handleOnNegativeButtonClick() {
        Log.d("20250228", "handleOnNegativeButtonClick")
        setSelectedDate(null)
    }

    override fun handleOnCancel() {
        Log.d("20250228", "handleOnCancel()")
        setSelectedDate(null)
    }

    private fun setSelectedDate(selectedDate: LocalDate?) {
        setResult(KEY_SELECTED_DATE, selectedDate)
    }
}
