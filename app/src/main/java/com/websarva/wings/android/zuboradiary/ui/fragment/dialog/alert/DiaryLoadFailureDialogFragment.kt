package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString

/**
 * 日記の読み込みに失敗したことを通知するための警告ダイアログ。
 */
class DiaryLoadFailureDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_load_failure_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryLoadFailureDialogFragmentArgs.fromBundle(requireArguments()).date
        val diaryDateString = diaryDate.formatDateString(requireContext())
        return diaryDateString + getString(R.string.dialog_diary_load_failure_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        // 処理なし(customizeDialog()でNegativeButton削除)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryLoadFailureDialogFragment::class.java.name
    }
}
