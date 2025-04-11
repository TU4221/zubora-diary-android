package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

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
        setSelectedDate(selectedDate)
    }

    override fun handleOnNegativeButtonClick() {
        setSelectedDate(null)
    }

    override fun handleOnCancel() {
        setSelectedDate(null)
    }

    private fun setSelectedDate(selectedDate: LocalDate?) {
        setResult(KEY_SELECTED_DATE, selectedDate)
    }
}
