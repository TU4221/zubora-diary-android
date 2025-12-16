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
        val title = createTitle()
        builder.setTitle(title)

        val message = createMessage()
        builder.setMessage(message)

        builder.setPositiveButton(R.string.dialog_base_alert_yes) { _: DialogInterface, _: Int ->
            Log.d(logTag, "onClick()_PositiveButton")
            handleOnPositiveButtonClick()
        }

        builder.setNegativeButton(R.string.dialog_base_alert_no) { _: DialogInterface, _: Int ->
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
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
     * ダイアログのタイトル文字列を生成する。[onCreateDialog]から呼び出される。
     */
    protected abstract fun createTitle(): String

    /**
     * ダイアログのメッセージ文字列を生成する。[onCreateDialog]から呼び出される。
     */
    protected abstract fun createMessage(): String

    /**
     * Positiveボタンがクリックされた際の処理を定義する。
     */
    protected abstract fun handleOnPositiveButtonClick()

    /**
     * Negativeボタンがクリックされた際の処理を定義する。
     */
    protected abstract fun handleOnNegativeButtonClick()

    // ダイアログ枠外タッチ、popBackStack時に処理
    // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
    //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
    //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
    //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
    /** 追加処理として、キャンセル時の独自の処理を呼び出す。 */
    override fun onCancel(dialog: DialogInterface) {
        Log.d(logTag, "onCancel()")
        handleOnCancel()
        super.onCancel(dialog)
    }

    /**
     * ダイアログがキャンセルされた際の処理を定義する。[onCancel]から呼び出される。
     */
    protected abstract fun handleOnCancel()
}
