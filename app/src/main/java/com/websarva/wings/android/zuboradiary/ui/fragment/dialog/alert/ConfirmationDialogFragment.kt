package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import androidx.navigation.fragment.navArgs
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.navigation.params.ConfirmationDialogArgs
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

/**
 * アプリ内で共通して使用される、汎用的な確認ダイアログ。
 * 表示する内容は [ConfirmationDialogArgs] を通じて外部から注入される。
 */
class ConfirmationDialogFragment : BaseAlertDialogFragment() {

    private val navArgs: ConfirmationDialogFragmentArgs by navArgs()
    private val dialogArgs: ConfirmationDialogArgs get() = navArgs.dialogArgs

    override fun createTitle(): String {
        return dialogArgs.titleText ?: getString(checkNotNull(dialogArgs.titleRes))
    }

    override fun createMessage(): String {
        return dialogArgs.messageText ?: getString(checkNotNull(dialogArgs.messageRes))
    }

    override fun handleOnPositiveButtonClick() {
        setResult(dialogArgs.resultKey, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(dialogArgs.resultKey, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(dialogArgs.resultKey, DialogResult.Cancel)
    }
}
