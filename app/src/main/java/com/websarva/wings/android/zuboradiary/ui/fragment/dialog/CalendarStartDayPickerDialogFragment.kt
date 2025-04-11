package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.utils.DayOfWeekStringConverter
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding
import java.time.DayOfWeek

class CalendarStartDayPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        private val FROM_CLASS_NAME = "From" + CalendarStartDayPickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_DAY_OF_WEEK: String = "SelectedDayOfWeek$FROM_CLASS_NAME"
    }

    override fun handleOnPositiveButtonClick() {
        setResultSelectedDayOfWeek()
    }

    private fun setResultSelectedDayOfWeek() {
        val selectedValue = binding.numberPickerFirst.value
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        val selectedDayOfWeek = if (selectedValue == 0) {
            DayOfWeek.SUNDAY
        } else {
            DayOfWeek.of(selectedValue)
        }
        setResult(KEY_SELECTED_DAY_OF_WEEK, selectedDayOfWeek)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_SELECTED_DAY_OF_WEEK, null)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_DAY_OF_WEEK, null)
    }

    override fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding) {
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
            val dayOfWeekStringConverter = DayOfWeekStringConverter(requireContext())
            dayOfWeekList[dayOfWeekNumber] =
                dayOfWeekStringConverter.toCalendarStartDayOfWeek(dayOfWeek)
        }
        binding.numberPickerFirst.displayedValues = dayOfWeekList
    }
}
