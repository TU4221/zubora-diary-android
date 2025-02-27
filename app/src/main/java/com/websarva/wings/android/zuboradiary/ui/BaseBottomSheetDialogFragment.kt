package com.websarva.wings.android.zuboradiary.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    protected lateinit var settingsViewModel: SettingsViewModel
        private set

    protected val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setUpDialogCancelFunction()

        settingsViewModel = createSettingsViewModel()

        val themeColorInflater = createThemeColorInflater(inflater, themeColor)
        return createDialogView(themeColorInflater, container, savedInstanceState)
    }

    private fun setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        isCancelable = false
    }

    private fun createSettingsViewModel(): SettingsViewModel {
        val provider = ViewModelProvider(requireActivity())
        return provider[SettingsViewModel::class.java]
    }

    // ThemeColorに合わせたインフレーター作成
    private fun createThemeColorInflater(
        inflater: LayoutInflater,
        themeColor: ThemeColor
    ): LayoutInflater {
        val creator = ThemeColorInflaterCreator(requireContext(), inflater, themeColor)
        return creator.create()
    }

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * BaseBottomSheetDialogFragment#onCreateView()で呼び出される。
     */
    protected abstract fun createDialogView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View?

    protected inner class PositiveButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            handleOnPositiveButtonClick(v)
            closeDialog()
        }
    }

    protected inner class NegativeButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            handleOnNegativeButtonClick(v)
            closeDialog()
        }
    }

    private fun closeDialog() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
    }

    /**
     * BaseBottomSheetDialogFragment.PositiveButtonClickListener#onClick()で呼び出される。
     */
    protected abstract fun handleOnPositiveButtonClick(v: View)

    /**
     * BaseBottomSheetDialogFragment.NegativeButtonClickListener#onClick()で呼び出される。
     */
    protected abstract fun handleOnNegativeButtonClick(v: View)

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
     * BaseBottomSheetDialogFragment.onCancel()で呼び出される。
     */
    protected abstract fun handleOnCancel(dialog: DialogInterface)

    override fun dismiss() {
        handleOnDismiss()
        super.dismiss()
    }

    /**
     * BaseBottomSheetDialogFragment.dismiss()で呼び出される。
     */
    protected abstract fun handleOnDismiss()
}
