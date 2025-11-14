package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.websarva.wings.android.zuboradiary.ui.utils.datePickerDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

abstract class BaseDatePickerDialogFragment : DialogFragment() {

    private val themeColor
        get() = (requireActivity() as MainActivity).themeColor

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
        // MEMO：MaterialDatePickerはUTC基準の為UTC基準で変換
        val initialEpochMilli =
            initialDate
                .atStartOfDay(ZoneOffset.UTC) // UTCでのその日の始まりの時刻(00:00)を取得
                .toInstant() // UTC基準の時点に変換
                .toEpochMilli()
        builder.setSelection(initialEpochMilli)

        val datePicker = builder.build()

        setupOnClickListener(datePicker, dummyDialog)

        return datePicker
    }

    protected abstract fun createInitialDate(): LocalDate

    private fun setupOnClickListener(datePicker: MaterialDatePicker<Long>, dummyDialog: Dialog) {
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
}
