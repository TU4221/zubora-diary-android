package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.DateTimeStringConverter

class DiaryLoadingFailureDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryLoadingFailureDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_loading_failure_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryLoadingFailureDialogFragmentArgs.fromBundle(requireArguments()).date
        val diaryDateString = DateTimeStringConverter().toYearMonthDayWeek(diaryDate)
        return diaryDateString + getString(R.string.dialog_diary_loading_failure_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick() {
        // 処理なし(customizeDialog()でNegativeButton削除)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        super.customizeDialog(builder)
        builder.setNegativeButton("", null)
    }
}
