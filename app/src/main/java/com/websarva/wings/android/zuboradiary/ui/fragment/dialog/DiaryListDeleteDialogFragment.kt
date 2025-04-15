package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.net.Uri
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import java.time.LocalDate

class DiaryListDeleteDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + DiaryListDeleteDialogFragment::class.java.name
        @JvmField
        val KEY_DELETE_DIARY_DATE: String = "DeleteDiaryDate$fromClassName"
        @JvmField
        val KEY_DELETE_DIARY_PICTURE_URI: String = "DeleteDiaryPictureUri$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_delete_title)
    }

    override fun createMessage(): String {
        val date = DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val strDate = date.toJapaneseDateString(requireContext())
        return strDate + getString(R.string.dialog_diary_delete_message)
    }

    override fun handleOnPositiveButtonClick() {

        val deleteDiaryDate =
            DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).date
        val deleteDiaryPictureUri =
            DiaryListDeleteDialogFragmentArgs.fromBundle(requireArguments()).pictureUri
        setResults(deleteDiaryDate, deleteDiaryPictureUri)
    }

    override fun handleOnNegativeButtonClick() {
        setResults()
    }

    override fun handleOnCancel() {
        setResults()
    }

    private fun setResults(date: LocalDate? = null, pictureUri: Uri? = null) {
        setResult(KEY_DELETE_DIARY_DATE, date)
        setResult(KEY_DELETE_DIARY_PICTURE_URI, pictureUri)
    }
}
