package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.websarva.wings.android.zuboradiary.ui.utils.timePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime

@AndroidEntryPoint
abstract class BaseTimePickerDialogFragment : DialogFragment() {

    private val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")

        // MEMO:MaterialTimePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialTimePicker#show()でTimePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialTimePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        val dummyDialog = Dialog(requireContext())

        val timePicker = createTimePickerDialog(dummyDialog)
        timePicker.show(childFragmentManager, "")

        return dummyDialog
    }

    private fun createTimePickerDialog(dummyDialog: Dialog): MaterialTimePicker {
        val builder = MaterialTimePicker.Builder()

        val themeResId = themeColor.timePickerDialogThemeResId
        builder.setTheme(themeResId)

        builder
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(INPUT_MODE_CLOCK)

        setupInitializationTime(builder)

        val timePicker = builder.build()

        setupOnClickListener(timePicker, dummyDialog)

        return timePicker
    }

    protected abstract fun setupInitializationTime(builder: MaterialTimePicker.Builder)

    private fun setupOnClickListener(timePicker: MaterialTimePicker, dummyDialog: Dialog) {
        timePicker.addOnPositiveButtonClickListener { _ ->
            Log.d(logTag, "onClick()_PositiveButton")

            // 選択日付型変換(Int -> LocalTime)
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val localTime = LocalTime.of(selectedHour, selectedMinute)
            handleOnPositiveButtonClick(localTime)
            dummyDialog.dismiss()
        }

        timePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
            dummyDialog.dismiss()
        }

        timePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            handleOnCancel()
            dummyDialog.dismiss()
        }
    }

    protected abstract fun handleOnPositiveButtonClick(selectedTime: LocalTime)

    protected abstract fun handleOnNegativeButtonClick()

    protected abstract fun handleOnCancel()
}
