package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import androidx.navigation.fragment.navArgs
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

/**
 * アプリ内で共通して使用される、汎用的な確認ダイアログ。
 * 表示する内容は Navigation Component の引数を通じて外部から注入される。
 */
class ConfirmationDialogFragment : BaseAlertDialogFragment() {

    /** 画面遷移時に渡された引数。 */
    private val navArgs: ConfirmationDialogFragmentArgs by navArgs()

    override fun createTitle(): String {
        return navArgs.params.titleText ?: getString(checkNotNull(navArgs.params.titleRes))
    }

    override fun createMessage(): String {
        return navArgs.params.messageText ?: getString(checkNotNull(navArgs.params.messageRes))
    }

    override fun handleOnPositiveButtonClick() {
        setResult(navArgs.params.resultKey, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(navArgs.params.resultKey, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(navArgs.params.resultKey, DialogResult.Cancel)
    }
}
