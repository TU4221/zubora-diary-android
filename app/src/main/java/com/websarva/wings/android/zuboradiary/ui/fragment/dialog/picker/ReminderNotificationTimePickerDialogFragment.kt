package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import com.google.android.material.timepicker.MaterialTimePicker
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalTime

class ReminderNotificationTimePickerDialogFragment : BaseTimePickerDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + ReminderNotificationTimePickerDialogFragment::class.java.name
    }

    override fun setUpInitializationTime(builder: MaterialTimePicker.Builder) {
        val localTime = LocalTime.now()
        builder.setHour(localTime.hour)
        builder.setMinute(localTime.minute)
    }

    override fun handleOnPositiveButtonClick(selectedTime: LocalTime) {
        setResult(KEY_RESULT, DialogResult.Positive(selectedTime))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
