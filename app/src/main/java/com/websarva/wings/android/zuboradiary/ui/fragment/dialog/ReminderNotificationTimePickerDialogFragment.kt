package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.content.DialogInterface
import com.google.android.material.timepicker.MaterialTimePicker
import java.time.LocalTime

class ReminderNotificationTimePickerDialogFragment : BaseTimePickerDialogFragment() {

    companion object {
        private val fromClassName =
            "From" + ReminderNotificationTimePickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
        @JvmField
        val KEY_SELECTED_TIME: String = "SelectedTIME$fromClassName"
    }

    override fun setUpInitializationTime(builder: MaterialTimePicker.Builder) {
        val localTime = LocalTime.now()
        builder.setHour(localTime.hour)
        builder.setMinute(localTime.minute)
    }

    override fun handleOnPositiveButtonClick(selectedTime: LocalTime) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
        setResult(KEY_SELECTED_TIME, selectedTime)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }
}
