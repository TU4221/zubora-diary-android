package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment

class DiaryDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_DELETE_DIARY_DATE: String = "DeleteDiaryDate$fromClassName"
        @JvmField
        val KEY_DELETE_DIARY_PICTURE_URI: String = "DeleteDiaryPictureUri$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_delete_title)
    }

    override fun createMessage(): String {
        val date = DiaryDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateTimeStringConverter = DateTimeStringConverter()
        val strDate = dateTimeStringConverter.toYearMonthDayWeek(date)
        return strDate + getString(R.string.dialog_diary_delete_message)
    }

    override fun handleOnPositiveButtonClick() {
        val deleteDiaryDate =
            DiaryDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        setResult(KEY_DELETE_DIARY_DATE, deleteDiaryDate)


        val deleteDiaryPictureUri =
            DiaryDeleteDialogFragmentArgs.fromBundle(requireArguments()).pictureUri
        setResult(
            KEY_DELETE_DIARY_PICTURE_URI,
            deleteDiaryPictureUri
        )
    }

    override fun handleOnNegativeButtonClick() {
        // 処理なし
    }

    override fun handleOnCancel() {
        // 処理なし
    }

    override fun handleOnDismiss() {
        // 処理なし
    }
}
