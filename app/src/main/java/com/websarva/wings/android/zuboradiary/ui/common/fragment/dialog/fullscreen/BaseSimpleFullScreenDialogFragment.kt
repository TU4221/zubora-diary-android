package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.main.MainActivity
import com.websarva.wings.android.zuboradiary.ui.common.fragment.FragmentHelper
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.enableEdgeToEdge

/**
 * シンプルな全画面ダイアログの基底クラス。
 *
 * 以下の責務を持つ:
 * - 全画面表示用のダイアログスタイルの設定
 * - [ViewBinding]のライフサイクル管理
 * - エッジ・ツー・エッジ表示の有効化とテーマカラーの適用
 * - 前の画面へ戻るナビゲーションヘルパーの提供
 */
abstract class BaseSimpleFullScreenDialogFragment<T: ViewBinding>: DialogFragment() {

    //region Properties
    /** [ViewBinding]のインスタンス。[onDestroyView]でnullに設定される。 */
    private var _binding: T? = null
    /** [ViewBinding]のインスタンスへの非nullアクセスを提供する。 */
    protected val binding get() = checkNotNull(_binding)

    /** Fragmentの共通処理をまとめたヘルパークラス。 */
    protected val fragmentHelper = FragmentHelper()

    /** [MainActivity]から取得する現在のテーマカラー。 */
    protected val themeColor
        get() = (requireActivity() as MainActivity).themeColor
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、全画面表示のダイアログスタイルを設定する。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MaterialFullScreenDialogTheme)
    }

    /** 追加処理として、テーマカラーを適用したInflaterでViewを生成する。 */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, container)
        return binding.root
    }

    /** 追加処理として、エッジ・ツー・エッジ表示の有効化とステータスバーのアイコンカラー設定を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableEdgeToEdge(themeColor)
        val dialogWindow = checkNotNull(dialog?.window)
        ThemeColorChanger().applyStatusBarIconColor(dialogWindow, themeColor)
    }

    /** 追加処理として、ダイアログのレイアウトを画面全体に広げる。 */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /** 追加処理として、[ViewBinding]の解放を行う。 */
    override fun onDestroyView() {
        super.onDestroyView()

        clearViewBindings()
    }
    //endregion

    //region View Binding Setup
    /**
     * [ViewBinding]インスタンスを生成する。[onCreateView] で呼び出される。
     * @param themeColorInflater テーマカラーが適用されたLayoutInflater
     * @param container 親のViewGroup
     * @return 生成されたViewBindingインスタンス
     */
    protected abstract fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): T

    /** [ViewBinding]のインスタンスを解放する。 [onDestroyView] で呼び出される。*/
    @CallSuper
    protected open fun clearViewBindings() {
        _binding = null
    }
    //endregion

    //region Navigation Helpers
    /** 前の画面へ遷移する。 */
    protected fun dismissWithNavigateUp() {
        findNavController().navigateUp()
    }
    //endregion
}
