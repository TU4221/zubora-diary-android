package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.FragmentHelper
import com.websarva.wings.android.zuboradiary.ui.utils.bottomSheetDialogThemeResId
import com.websarva.wings.android.zuboradiary.ui.utils.enableEdgeToEdge
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity

abstract class BaseBottomSheetDialogFragment<T: ViewBinding> : BottomSheetDialogFragment() {

    //region Properties
    private var _binding: T? = null
    protected val binding get() = checkNotNull(_binding)

    protected val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    private val fragmentHelper = FragmentHelper()
    //endregion

    //region Fragment Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, themeColor.bottomSheetDialogThemeResId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag, "onCreateView()")
        super.onCreateView(inflater, container, savedInstanceState)

        setUpDialogCancelFunction()

        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableEdgeToEdge(themeColor)
    }

    override fun onDestroyView() {
        Log.d(logTag, "onDestroyView()")
        clearViewBindings()

        super.onDestroyView()
    }
    //endregion

    //region Dialog Setup
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog.apply {
            // MEMO:表示するLayoutによっては折り畳み状態で表示される為、固定で展開状態で表示する。
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        isCancelable = true
    }

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
    //endregion

    //region View Binding Setup
    /**
     * BaseBottomSheetDialogFragment#onCreateView()で呼び出される。
     */
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    private fun clearViewBindings() {
        _binding = null
    }
    //endregion

    //region Navigation Helpers
    protected fun navigatePreviousFragment() {
        findNavController().navigateUp()
    }
    //endregion
}
