package com.websarva.wings.android.zuboradiary.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


abstract class BaseDatePickerDialogFragment : DialogFragment() {

    protected lateinit var settingsViewModel: SettingsViewModel

    private val themeColor
        get() = settingsViewModel.themeColor.checkNotNull()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        settingsViewModel = createSettingsViewModel()

        // MEMO:MaterialDatePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialDatePicker#show()でDatePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialDatePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        val dummyDialog = Dialog(requireContext())

        val datePicker = createDatePickerDialog(dummyDialog)
        datePicker.show(childFragmentManager, "")

        return dummyDialog
    }

    private fun createSettingsViewModel(): SettingsViewModel {
        val provider = ViewModelProvider(requireActivity())
        return provider[SettingsViewModel::class.java]
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
            // 選択日付型変換(EpochMilli -> LocalDate)
            val instant = Instant.ofEpochMilli(selection)
            val selectedDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
            handleOnPositiveButtonClick(selectedDate)
            dummyDialog.dismiss()
        }

        datePicker.addOnNegativeButtonClickListener { v: View ->
            handleOnNegativeButtonClick(v)
            dummyDialog.dismiss()
        }

        datePicker.addOnCancelListener { dialog: DialogInterface ->
            handleOnCancel(dialog)
            dummyDialog.dismiss()
        }

        datePicker.addOnDismissListener { dialog: DialogInterface ->
            handleOnDismiss(dialog)
            dummyDialog.dismiss()
        }
    }

    protected abstract fun handleOnPositiveButtonClick(selectedDate: LocalDate)

    protected abstract fun handleOnNegativeButtonClick(v: View)

    protected abstract fun handleOnCancel(dialog: DialogInterface)

    protected abstract fun handleOnDismiss(dialog: DialogInterface)
}
