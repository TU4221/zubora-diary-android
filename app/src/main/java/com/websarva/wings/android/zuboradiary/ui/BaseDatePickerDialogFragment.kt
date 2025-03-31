package com.websarva.wings.android.zuboradiary.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.websarva.wings.android.zuboradiary.getLogTag
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


abstract class BaseDatePickerDialogFragment : DialogFragment() {

    private val logTag = getLogTag()

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    protected val settingsViewModel: SettingsViewModel by activityViewModels()

    private val themeColor
        get() = settingsViewModel.themeColor.requireValue()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")

        // MEMO:MaterialDatePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialDatePicker#show()でDatePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialDatePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        val dummyDialog = Dialog(requireContext())

        val datePicker = createDatePickerDialog(dummyDialog)
        datePicker.show(childFragmentManager, "")

        return dummyDialog
    }

    private fun createDatePickerDialog(dummyDialog: Dialog): MaterialDatePicker<Long> {
        val builder = MaterialDatePicker.Builder.datePicker()

        val themeResId = themeColor.datePickerDialogThemeResId
        builder.setTheme(themeResId)

        val initialDate = createInitialDate()
        val initialEpochMilli =
            initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        builder.setSelection(initialEpochMilli)

        val datePicker = builder.build()

        setUpOnClickListener(datePicker, dummyDialog)

        return datePicker
    }

    protected abstract fun createInitialDate(): LocalDate

    private fun setUpOnClickListener(datePicker: MaterialDatePicker<Long>, dummyDialog: Dialog) {
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            Log.d(logTag, "onClick()_PositiveButton")

            // 選択日付型変換(EpochMilli -> LocalDate)
            val instant = Instant.ofEpochMilli(selection)
            val selectedDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
            handleOnPositiveButtonClick(selectedDate)
            dummyDialog.dismiss()
        }

        datePicker.addOnNegativeButtonClickListener {
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
            dummyDialog.dismiss()
        }

        datePicker.addOnCancelListener {
            Log.d(logTag, "onCancel()")
            handleOnCancel()
            dummyDialog.dismiss()
        }
    }

    protected abstract fun handleOnPositiveButtonClick(selectedDate: LocalDate)

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
