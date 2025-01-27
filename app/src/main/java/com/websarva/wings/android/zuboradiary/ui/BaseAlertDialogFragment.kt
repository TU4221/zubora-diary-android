package com.websarva.wings.android.zuboradiary.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel
import java.util.Objects

abstract class BaseAlertDialogFragment : DialogFragment() {

    protected lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        settingsViewModel = createSettingsViewModel()

        val themeResId = requireThemeColor().alertDialogThemeResId
        val builder = MaterialAlertDialogBuilder(requireContext(), themeResId)

        customizeDialog(builder)

        val alertDialog = builder.create()

        setUpDialogCancelFunction(alertDialog)

        return alertDialog
    }

    private fun createSettingsViewModel(): SettingsViewModel {
        val provider = ViewModelProvider(requireActivity())
        val settingsViewModel = provider[SettingsViewModel::class.java]
        return Objects.requireNonNull(settingsViewModel)
    }

    protected fun requireThemeColor(): ThemeColor {
        return settingsViewModel.loadThemeColorSettingValue()
    }

    protected open fun customizeDialog(builder: MaterialAlertDialogBuilder) {
        val title = createTitle()
        builder.setTitle(title)

        val message = createMessage()
        builder.setMessage(message)

        builder.setPositiveButton(R.string.dialog_base_alert_yes) { dialog: DialogInterface, which: Int ->
            handleOnPositiveButtonClick(dialog, which)
        }

        builder.setNegativeButton(R.string.dialog_base_alert_no) { dialog: DialogInterface, which: Int ->
            handleOnNegativeButtonClick(dialog, which)
        }
    }

    private fun setUpDialogCancelFunction(alertDialog: AlertDialog) {
        // MEMO:下記機能を無効にするにはAlertDialog#setCanceledOnTouchOutside、DialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)(AlertDialog#setCanceledOnTouchOutside)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)(DialogFragment#setCancelable)
        if (!isCancelableOtherThanPressingButton) {
            alertDialog.setCanceledOnTouchOutside(false)
            this.isCancelable = false
        }
    }

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    protected abstract fun createTitle(): String

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    protected abstract fun createMessage(): String

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    protected abstract fun handleOnPositiveButtonClick(dialog: DialogInterface, which: Int)

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     */
    protected abstract fun handleOnNegativeButtonClick(dialog: DialogInterface, which: Int)

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     *
     * @noinspection SameReturnValue
     */
    protected abstract val isCancelableOtherThanPressingButton: Boolean

    // ダイアログ枠外タッチ、popBackStack時に処理
    // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
    //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
    //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
    //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
    override fun onCancel(dialog: DialogInterface) {
        handleOnCancel(dialog)
        super.onCancel(dialog)
    }

    /**
     * BaseAlertDialogFragment.onCancel()で呼び出される。
     */
    protected abstract fun handleOnCancel(dialog: DialogInterface)

    override fun dismiss() {
        handleOnDismiss()
        super.dismiss()
    }

    /**
     * BaseAlertDialogFragment.dismiss()で呼び出される。
     */
    protected abstract fun handleOnDismiss()

    protected fun setResult(resultKey: String, result: Any) {
        val navController = NavHostFragment.findNavController(this)
        val navBackStackEntry = requireNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle

        savedStateHandle[resultKey] = result
    }
}
