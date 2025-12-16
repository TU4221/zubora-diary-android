package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet

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
import com.websarva.wings.android.zuboradiary.ui.common.fragment.FragmentHelper
import com.websarva.wings.android.zuboradiary.ui.common.theme.bottomSheetDialogThemeResId
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.enableEdgeToEdge
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity

/**
 * [BottomSheetDialogFragment]をベースとした、シンプルなボトムシートダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - [BottomSheetDialog]の基本的な設定（テーマ、展開状態）
 * - [ViewBinding]のライフサイクル管理
 * - エッジ・ツー・エッジ表示の有効化
 * - キャンセル時のコールバック処理
 *
 * @param T ViewBindingの型
 */
abstract class BaseBottomSheetDialogFragment<T: ViewBinding> : BottomSheetDialogFragment() {

    //region Properties
    /** [ViewBinding]のインスタンス。[onDestroyView]でnullに設定される。 */
    private var _binding: T? = null
    /** [ViewBinding]のインスタンスへの非nullアクセスを提供する。 */
    protected val binding get() = checkNotNull(_binding)

    /** [MainActivity]から取得する現在のテーマカラー。 */
    protected val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    /** Fragmentの共通処理をまとめたヘルパークラス。 */
    private val fragmentHelper = FragmentHelper()
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、ダイアログのスタイルを設定する。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, themeColor.bottomSheetDialogThemeResId)
    }

    /** 追加処理として、キャンセル設定と[ViewBinding]の初期化を行う。 */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag, "onCreateView()")
        super.onCreateView(inflater, container, savedInstanceState)

        enableDialogCancellation()

        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, container)
        return binding.root
    }

    /** 追加処理として、エッジ・ツー・エッジ表示を有効化する。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableEdgeToEdge(themeColor)
    }

    /** 追加処理として、[ViewBinding]の解放を行う。 */
    override fun onDestroyView() {
        Log.d(logTag, "onDestroyView()")
        clearViewBindings()

        super.onDestroyView()
    }
    //endregion

    //region Dialog Setup
    /** 追加処理として、ダイアログを常に展開した状態で表示するよう設定する。 */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog.apply {
            // MEMO:表示するLayoutによっては折り畳み状態で表示される為、固定で展開状態で表示する。
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    /** ダイアログのキャンセル（ダイアログ外タップ、戻るボタン）を有効に設定する。 */
    private fun enableDialogCancellation() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        isCancelable = true
    }

    /** 追加処理として、キャンセル時の独自のコールバック処理を呼び出す。 */
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
     * ダイアログがキャンセルされた際の処理を定義する。
     */
    protected abstract fun handleOnCancel()
    //endregion

    //region View Binding Setup
    /**
     * [ViewBinding]インスタンスを生成する。[onCreateView] で呼び出される。
     * @param inflater テーマカラーが適用されたLayoutInflater
     * @param container 親のViewGroup
     * @return 生成されたViewBindingインスタンス
     */
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    /** [ViewBinding]のインスタンスを解放する。[onDestroyView] で呼び出される。 */
    protected open fun clearViewBindings() {
        _binding = null
    }
    //endregion

    //region Navigation Helpers
    /** 前の画面へ遷移する。 */
    protected fun navigatePreviousFragment() {
        findNavController().navigateUp()
    }
    //endregion
}
