package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.websarva.wings.android.zuboradiary.ui.BaseDatePickerDialogFragment
import java.time.LocalDate


class DatePickerDialogFragment : BaseDatePickerDialogFragment() {

    companion object {
        private val fromClassName = "From" + DatePickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_DATE: String = "SelectedDate$fromClassName"
    }

    override fun createInitialDate(): LocalDate {
        return DatePickerDialogFragmentArgs.fromBundle(requireArguments()).date
    }

    override fun handleOnPositiveButtonClick(selectedDate: LocalDate) {
        // 選択日付を返す
        val navController =
            NavHostFragment.findNavController(this@DatePickerDialogFragment)
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_SELECTED_DATE] = selectedDate
    }

    override fun handleOnNegativeButtonClick(v: View) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss(dialog: DialogInterface) {
        // 処理なし
    }
}
