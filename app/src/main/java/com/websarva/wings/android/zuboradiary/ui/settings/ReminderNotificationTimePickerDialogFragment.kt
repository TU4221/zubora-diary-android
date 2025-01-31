package com.websarva.wings.android.zuboradiary.ui.settings

import android.content.DialogInterface
import android.view.View
import android.widget.NumberPicker
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment
import java.time.LocalTime
import java.util.Locale

// TODO:MaterialTimePickerに置き換える？
class ReminderNotificationTimePickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        private val fromClassName =
            "From" + ReminderNotificationTimePickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
        @JvmField
        val KEY_SELECTED_TIME: String = "SelectedTIME$fromClassName"
    }

    override val isCancelableOtherThanPressingButton: Boolean
        get() = true

    override fun handleOnPositiveButtonClick(v: View) {
        setResultSelectedYearMonth()
    }

    private fun setResultSelectedYearMonth() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)

        val selectedHourValue = binding.numberPickerFirst.value
        val selectedMinuteValue = binding.numberPickerSecond.value
        val selectedTime = LocalTime.of(selectedHourValue, selectedMinuteValue)
        setResult(KEY_SELECTED_TIME, selectedTime)
    }

    override fun handleOnNegativeButtonClick(v: View) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnDismiss() {
        // 処理なし
    }

    override fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding) {
        val localTime = LocalTime.now()
        binding.numberPickerFirst.maxValue = 23
        binding.numberPickerFirst.minValue = 0
        binding.numberPickerFirst.setFormatter(ValueFormatter())
        binding.numberPickerFirst.wrapSelectorWheel = false
        binding.numberPickerFirst.value = localTime.hour
        binding.numberPickerSecond.maxValue = 59
        binding.numberPickerSecond.minValue = 0
        binding.numberPickerSecond.setFormatter(ValueFormatter())
        binding.numberPickerSecond.wrapSelectorWheel = false
        binding.numberPickerSecond.value = localTime.minute
        binding.numberPickerThird.visibility = View.GONE
    }

    private class ValueFormatter : NumberPicker.Formatter {
        override fun format(value: Int): String {
            return String.format(Locale.getDefault(), "%02d", value)
        }
    }
}
