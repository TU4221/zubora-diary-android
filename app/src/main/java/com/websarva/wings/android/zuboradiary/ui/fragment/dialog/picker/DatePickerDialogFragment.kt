package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker

import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import java.time.LocalDate

/**
 * 日付を選択するための日付選択ダイアログ。
 */
class DatePickerDialogFragment : BaseDatePickerDialogFragment() {

    override fun createInitialDate(): LocalDate {
        return DatePickerDialogFragmentArgs.fromBundle(requireArguments()).date
    }

    override fun handleOnPositiveButtonClick(selectedDate: LocalDate) {
        setResult(RESULT_KEY, DialogResult.Positive(selectedDate))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DatePickerDialogFragment::class.java.name
    }
}
