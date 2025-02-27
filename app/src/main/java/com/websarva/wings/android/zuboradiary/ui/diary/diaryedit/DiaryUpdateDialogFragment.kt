package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

class DiaryUpdateDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryUpdateDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_update_title)
    }

    override fun createMessage(): String {
        val updateDiaryDate =
            DiaryUpdateDialogFragmentArgs.fromBundle(requireArguments()).date
        val converter = DateTimeStringConverter()
        val updateDiaryDateString = converter.toYearMonthDayWeek(updateDiaryDate)
        return updateDiaryDateString + getString(R.string.dialog_diary_update_message)
    }

    override fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick(dialog: DialogInterface, which: Int) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss() {
        // 処理なし
    }
}
