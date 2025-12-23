package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.alert

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.theme.alertDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity

/**
 * MaterialAlertDialogBuilderを使用した基本的な警告ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - MaterialAlertDialogBuilderの基本的な設定（タイトル、メッセージ、ボタン）
 * - テーマカラーに応じたダイアログスタイルの適用
 * - Positive/Negativeボタンクリック、およびキャンセル時のコールバック処理
 */
abstract class BaseAlertDialogFragment : DialogFragment() {

    /** [MainActivity]から取得する現在のテーマカラー。 */
    private val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    /** ダイアログのタイトル文字列。 */
    protected abstract val title: String

    /** ダイアログのメッセージ文字列。 */
    protected abstract val message: String

    /**
     * Positiveボタンのテキスト。
     */
    protected open val positiveButtonText: String
        get() = getString(R.string.dialog_alert_positive)

    /**
     * Negativeボタンのテキスト。
     * null を返した場合、Negativeボタンは表示されない。
     */
    protected open val negativeButtonText: String?
        get() = getString(R.string.dialog_alert_negative)

    /** 追加処理として、テーマカラーに基づいたMaterialAlertDialogBuilderを生成し、ダイアログのカスタマイズを行う。 */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")
        super.onCreateDialog(savedInstanceState)

        val themeResId = themeColor.alertDialogThemeResId
        val builder = MaterialAlertDialogBuilder(requireContext(), themeResId)
        customizeDialog(builder)

        enableDialogCancellation()

        return builder.create()
    }

    /**
     * MaterialAlertDialogBuilderを使用してダイアログの各要素をカスタマイズする。
     * @param builder カスタマイズ対象のMaterialAlertDialogBuilder
     */
    @CallSuper
    protected open fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        builder.setTitle(title)
        builder.setMessage(message)

        // Positiveボタンの設定 (nullならセットしない = 非表示)
        positiveButtonText.let { text ->
            builder.setPositiveButton(text) { _: DialogInterface, _: Int ->
                Log.d(logTag, "onClick()_PositiveButton")
                handleOnPositiveButtonClick()
            }
        }

        // Negativeボタンの設定 (nullならセットしない = 非表示)
        negativeButtonText?.let { text ->
            builder.setNegativeButton(text) { _: DialogInterface, _: Int ->
                Log.d(logTag, "onClick()_NegativeButton")
                handleOnNegativeButtonClick()
            }
        }
    }

    /** ダイアログのキャンセル（ダイアログ外タップ、戻るボタン）を有効に設定する。 */
    private fun enableDialogCancellation() {
        // MEMO:下記機能を無効にするにはAlertDialog#setCanceledOnTouchOutside、DialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)(AlertDialog#setCanceledOnTouchOutside)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)(DialogFragment#setCancelable)
        isCancelable = true
    }

    /**
     * Positiveボタンがクリックされた際の処理。
     * ボタンを表示する場合、必要に応じてオーバーライドする。
     */
    protected open fun handleOnPositiveButtonClick() {
        // デフォルトでは何もしない
    }

    /**
     * Negativeボタンがクリックされた際の処理。
     * ボタンを表示する場合、必要に応じてオーバーライドする。
     */
    protected open fun handleOnNegativeButtonClick() {
        // デフォルトでは何もしない
    }

    /** 追加処理として、キャンセル時の独自の処理を呼び出す。 */
    override fun onCancel(dialog: DialogInterface) {
        Log.d(logTag, "onCancel()")
        handleOnCancel()
        super.onCancel(dialog)
    }

    /**
     * ダイアログがキャンセルされた際の処理を定義する。[onCancel]から呼び出される。
     * キャンセル動作はUI上のボタン有無に関わらず発生しうるため、abstractのまま(必須実装)とする。
     */
    protected abstract fun handleOnCancel()
}
