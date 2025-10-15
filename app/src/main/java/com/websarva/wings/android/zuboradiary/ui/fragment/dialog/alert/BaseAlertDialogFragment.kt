package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.alertDialogThemeResId
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import com.websarva.wings.android.zuboradiary.utils.logTag

abstract class BaseAlertDialogFragment : DialogFragment() {

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(logTag, "onCreateDialog()")
        super.onCreateDialog(savedInstanceState)

        val themeResId = themeColor.alertDialogThemeResId
        val builder = MaterialAlertDialogBuilder(requireContext(), themeResId)
        customizeDialog(builder)

        setUpDialogCancelFunction()

        return builder.create()
    }

    internal open fun customizeDialog(builder: MaterialAlertDialogBuilder) {
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

    private fun setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはAlertDialog#setCanceledOnTouchOutside、DialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)(AlertDialog#setCanceledOnTouchOutside)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)(DialogFragment#setCancelable)
        isCancelable = true
    }

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    internal abstract fun createTitle(): String

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    internal abstract fun createMessage(): String

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    internal abstract fun handleOnPositiveButtonClick()

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    internal abstract fun handleOnNegativeButtonClick()

    // ダイアログ枠外タッチ、popBackStack時に処理
    // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
    //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
    //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
    //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
    override fun onCancel(dialog: DialogInterface) {
        Log.d(logTag, "onCancel()")
        handleOnCancel()
        super.onCancel(dialog)
    }

    /**
     * BaseAlertDialogFragment.onCancel()で呼び出される。
     */
    internal abstract fun handleOnCancel()
}
