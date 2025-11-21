package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.databinding.DialogNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.asCalendarStartDayOfWeekString
import java.time.DayOfWeek

/**
 * カレンダーの週の開始曜日を選択するためのボトムシートダイアログ。
 */
class CalendarStartDayPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    override fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    ) {
        setResultSelectedDayOfWeek(firstPickerValue)
    }

    /**
     * NumberPickerで選択された値を[DayOfWeek]に変換し、結果として設定する。
     * @param pickerValue NumberPickerから取得した値
     */
    private fun setResultSelectedDayOfWeek(
        pickerValue: Int
    ) {
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        val selectedDayOfWeek =
            if (pickerValue == 0) {
                DayOfWeek.SUNDAY
            } else {
                DayOfWeek.of(pickerValue)
            }
        setResult(RESULT_KEY, DialogResult.Positive(selectedDayOfWeek))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    override fun setupNumberPickers() {
        val maxNumDaysOfWeek = DayOfWeek.entries.size
        binding.numberPickerFirst.maxValue = maxNumDaysOfWeek - 1
        binding.numberPickerFirst.minValue = 0
        setupInitialValue(binding)
        setupDisplayedValues(binding)
        binding.numberPickerFirst.wrapSelectorWheel = false
        binding.numberPickerSecond.visibility = View.GONE
        binding.numberPickerThird.visibility = View.GONE
    }

    /**
     * NumberPickerの初期値を設定する。
     * @param binding NumberPickerを含むViewBinding
     */
    private fun setupInitialValue(binding: DialogNumberPickersBinding) {
        val currentCalendarStartDayOfWeek =
            CalendarStartDayPickerDialogFragmentArgs.fromBundle(requireArguments()).initialValue
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        val initialValue = if (currentCalendarStartDayOfWeek == DayOfWeek.SUNDAY) {
            0
        } else {
            currentCalendarStartDayOfWeek.value
        }
        binding.numberPickerFirst.value = initialValue // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
    }

    /**
     * NumberPickerに表示する曜日の文字列を設定する。
     * @param binding NumberPickerを含むViewBinding
     */
    private fun setupDisplayedValues(binding: DialogNumberPickersBinding) {
        val maxNumDaysOfWeek = DayOfWeek.entries.size
        val dayOfWeekList = arrayOfNulls<String>(maxNumDaysOfWeek)
        for (dayOfWeek in DayOfWeek.entries) {
            // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
            val dayOfWeekNumber = if (dayOfWeek == DayOfWeek.SUNDAY) {
                0
            } else {
                dayOfWeek.value
            }
            dayOfWeekList[dayOfWeekNumber] =
                dayOfWeek.asCalendarStartDayOfWeekString(requireContext())
        }
        binding.numberPickerFirst.displayedValues = dayOfWeekList
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + CalendarStartDayPickerDialogFragment::class.java.name
    }
}
