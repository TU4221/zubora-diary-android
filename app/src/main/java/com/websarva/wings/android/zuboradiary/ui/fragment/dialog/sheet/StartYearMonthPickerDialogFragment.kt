package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalDate
import java.time.YearMonth

class StartYearMonthPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + StartYearMonthPickerDialogFragment::class.java.name
    }

    override fun handleOnPositiveButtonClick() {
        setResultSelectedYearMonth()
    }

    private fun setResultSelectedYearMonth() {
        val selectedYear = binding.numberPickerFirst.value
        val selectedMonth = binding.numberPickerSecond.value
        val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)

        setResult(KEY_RESULT, DialogResult.Positive(selectedYearMonth))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
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
