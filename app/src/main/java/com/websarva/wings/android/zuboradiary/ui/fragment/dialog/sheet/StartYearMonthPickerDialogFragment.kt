package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalDate
import java.time.YearMonth

/**
 * 日記リストの開始年月を選択するためのボトムシートダイアログ。
 */
class StartYearMonthPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    override fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    ) {
        setResultSelectedYearMonth(firstPickerValue, secondPickerValue)
    }

    /**
     * NumberPickerで選択された年月に基づいて結果を設定する。
     * @param yearPickerValue 年のNumberPickerから取得した値
     * @param monthPickerValue 月のNumberPickerから取得した値
     */
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

    override fun setupNumberPickers() {
        val today = LocalDate.now()
        val maxYear =
            StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).maxYear
        val minYear =
            StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).minYear
        with(binding) {
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

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + StartYearMonthPickerDialogFragment::class.java.name
    }
}
