package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val logTag = createLogTag()

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    internal val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog.apply {
            // MEMO:表示するLayoutによっては折り畳み状態で表示される為、固定で展開状態で表示する。
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag, "onCreateView()")
        super.onCreateView(inflater, container, savedInstanceState)

        setUpDialogCancelFunction()

        val themeColorInflater = createThemeColorInflater(inflater, themeColor)
        return createDialogView(themeColorInflater, container)
    }

    private fun setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        isCancelable = true
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
    protected abstract fun createDialogView(inflater: LayoutInflater, container: ViewGroup?): View?

    protected inner class PositiveButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d(logTag, "onClick()_PositiveButton")
            handleOnPositiveButtonClick()
            closeDialog()
        }
    }

    protected inner class NegativeButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d(logTag, "onClick()_NegativeButton")
            handleOnNegativeButtonClick()
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
    protected abstract fun handleOnPositiveButtonClick()

    /**
     * BaseBottomSheetDialogFragment.NegativeButtonClickListener#onClick()で呼び出される。
     */
    protected abstract fun handleOnNegativeButtonClick()

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
     * BaseBottomSheetDialogFragment.onCancel()で呼び出される。
     */
    protected abstract fun handleOnCancel()

    protected fun setResult(resultKey: String, result: Any?) {
        val navController = NavHostFragment.findNavController(this)
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle

        savedStateHandle[resultKey] = result
    }
}
