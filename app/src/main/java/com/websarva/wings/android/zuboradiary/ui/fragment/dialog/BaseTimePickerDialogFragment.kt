package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import java.time.LocalTime


abstract class BaseTimePickerDialogFragment : DialogFragment() {

    private val logTag = createLogTag()

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    private val themeColor
        get() = settingsViewModel.themeColor.requireValue()

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

        setUpInitializationTime(builder)

        val timePicker = builder.build()

        setUpOnClickListener(timePicker, dummyDialog)

        return timePicker
    }

    protected abstract fun setUpInitializationTime(builder: MaterialTimePicker.Builder)

    private fun setUpOnClickListener(timePicker: MaterialTimePicker, dummyDialog: Dialog) {
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

    @Suppress("SameParameterValue")
    protected fun setResult(resultKey: String, result: Any?) {
        val navController = NavHostFragment.findNavController(this)
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle

        savedStateHandle[resultKey] = result
    }
}
