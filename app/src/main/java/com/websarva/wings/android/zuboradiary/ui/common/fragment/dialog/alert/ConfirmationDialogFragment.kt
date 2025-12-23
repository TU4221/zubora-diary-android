package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.alert

import androidx.navigation.fragment.navArgs
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.setResult
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.DialogResult

/**
 * アプリ内で共通して使用される、汎用的な確認ダイアログ。
 * 表示する内容は Navigation Component の引数を通じて外部から注入される。
 */
class ConfirmationDialogFragment() : BaseAlertDialogFragment() {

    /** 画面遷移時に渡された引数。 */
    private val navArgs: ConfirmationDialogFragmentArgs by navArgs()


    override val title: String
        get() {
            val params = navArgs.params
            return params.titleText
                ?: params.titleRes?.let { getString(it) }
                ?: throw IllegalArgumentException("タイトルなし")
        }

    override val message: String
        get() {
            val params = navArgs.params
            return params.messageText
                ?: params.messageRes?.let { getString(it) }
                ?: throw IllegalArgumentException("メッセージなし")
        }

        override val positiveButtonText: String
        get() {
            val params = navArgs.params
            return params.positiveButtonText
                ?: getString(params.positiveButtonRes)
        }

    override val negativeButtonText: String?
        get() {
            val params = navArgs.params
            return params.negativeButtonText
                ?: params.negativeButtonRes?.let { getString(it) }
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
