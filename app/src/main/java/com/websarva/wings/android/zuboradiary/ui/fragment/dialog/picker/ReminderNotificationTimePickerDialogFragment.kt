package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import com.google.android.material.timepicker.MaterialTimePicker
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalTime

class ReminderNotificationTimePickerDialogFragment : BaseTimePickerDialogFragment() {

    override fun setupInitializationTime(builder: MaterialTimePicker.Builder) {
        val localTime = LocalTime.now()
        builder.setHour(localTime.hour)
        builder.setMinute(localTime.minute)
    }

    override fun handleOnPositiveButtonClick(selectedTime: LocalTime) {
        setResult(RESULT_KEY, DialogResult.Positive(selectedTime))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    internal companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + ReminderNotificationTimePickerDialogFragment::class.java.name
    }
}
