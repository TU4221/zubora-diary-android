package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.toCalendarStartDayOfWeekString
import java.time.DayOfWeek

class CalendarStartDayPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + CalendarStartDayPickerDialogFragment::class.java.name
    }

    override fun handleOnPositiveButtonClick() {
        setResultSelectedDayOfWeek()
    }

    private fun setResultSelectedDayOfWeek() {
        // TODO:handleOnPositiveButtonClick()の引数から選択値を受け取るように変更
        val selectedValue = binding.numberPickerFirst.value
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        val selectedDayOfWeek = if (selectedValue == 0) {
            DayOfWeek.SUNDAY
        } else {
            DayOfWeek.of(selectedValue)
        }
        setResult(KEY_RESULT, DialogResult.Positive(selectedDayOfWeek))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }

    override fun setUpNumberPickers() {
        val maxNumDaysOfWeek = DayOfWeek.entries.size
        binding.numberPickerFirst.maxValue = maxNumDaysOfWeek - 1
        binding.numberPickerFirst.minValue = 0
        setUpInitialValue(binding)
        setUpDisplayedValues(binding)
        binding.numberPickerFirst.wrapSelectorWheel = false
        binding.numberPickerSecond.visibility = View.GONE
        binding.numberPickerThird.visibility = View.GONE
    }

    private fun setUpInitialValue(binding: DialogFragmentNumberPickersBinding) {
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

    private fun setUpDisplayedValues(binding: DialogFragmentNumberPickersBinding) {
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
                dayOfWeek.toCalendarStartDayOfWeekString(requireContext())
        }
        binding.numberPickerFirst.displayedValues = dayOfWeekList
    }
}
