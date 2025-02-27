package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.content.DialogInterface
import android.view.View
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment
import java.time.LocalDate
import java.time.YearMonth

class StartYearMonthPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        private val fromClassName = "From" + StartYearMonthPickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_YEAR_MONTH: String = "SelectedYearMonth$fromClassName"
    }

    override fun handleOnPositiveButtonClick(v: View) {
        setResultSelectedYearMonth()
    }

    private fun setResultSelectedYearMonth() {
        val selectedYear = binding.numberPickerFirst.value
        val selectedMonth = binding.numberPickerSecond.value
        val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)

        setResult(KEY_SELECTED_YEAR_MONTH, selectedYearMonth)
    }

    override fun handleOnNegativeButtonClick(v: View) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss() {
        // 処理なし
    }

    override fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding) {
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
