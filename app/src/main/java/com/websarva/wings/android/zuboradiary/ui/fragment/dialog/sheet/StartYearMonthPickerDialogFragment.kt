package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalDate
import java.time.YearMonth

class StartYearMonthPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        @JvmField
        val RESULT_KEY = RESULT_KEY_PREFIX + StartYearMonthPickerDialogFragment::class.java.name
    }

    override fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    ) {
        setResultSelectedYearMonth(firstPickerValue, secondPickerValue)
    }

    private fun setResultSelectedYearMonth(
        yearPickerValue: Int,
        monthPickerValue: Int
    ) {
        val selectedYearMonth = YearMonth.of(yearPickerValue, monthPickerValue)

        setResult(RESULT_KEY, DialogResult.Positive(selectedYearMonth))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    override fun setUpNumberPickers() {
        val today = LocalDate.now()
        val maxYear =
            StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).maxYear
        val minYear =
            StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).minYear
        binding.apply {
            numberPickerFirst.maxValue = maxYear.value
            numberPickerFirst.minValue = minYear.value
            numberPickerFirst.value = today.year
            numberPickerFirst.wrapSelectorWheel = false
            numberPickerSecond.maxValue = 12
            numberPickerSecond.minValue = 1
            numberPickerSecond.value = today.monthValue
            numberPickerSecond.wrapSelectorWheel = false
            numberPickerThird.visibility = View.GONE
        }
    }
}
