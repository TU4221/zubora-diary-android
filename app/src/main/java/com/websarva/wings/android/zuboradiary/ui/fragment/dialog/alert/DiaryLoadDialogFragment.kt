package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString

/**
 * 既に日記が存在する日付を選択した際に、その日記を読み込むか確認するための警告ダイアログ。
 */
class DiaryLoadDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_diary_load_title)
    }

    override fun createMessage(): String {
        val diaryDate =
            DiaryLoadDialogFragmentArgs.fromBundle(requireArguments()).date
        val diaryDateString = diaryDate.formatDateString(requireContext())
        return diaryDateString + getString(R.string.dialog_diary_load_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryLoadDialogFragment::class.java.name
    }
}
