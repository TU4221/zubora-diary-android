package com.websarva.wings.android.zuboradiary.ui.common.keyboard

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.websarva.wings.android.zuboradiary.core.utils.logTag

/**
 * ソフトウェアキーボードの表示・非表示、および状態の監視を管理するクラス。
 *
 * 以下の責務を持つ:
 * - [InputMethodManager]を利用したキーボードの表示・非表示の制御
 * - [WindowInsetsCompat]を利用したキーボードの表示状態の監視とリスナーへの通知
 * - [Fragment]のライフサイクルに合わせたリスナーの安全な登録と解除
 *
 * @param context コンテキスト
 */
internal class KeyboardManager(
    context: Context
) {

    private val inputMethodManager =
        context.applicationContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    /**
     * ソフトウェアキーボードを非表示にする。
     * @param focusView 現在フォーカスを持っているビュー
     */
    fun hideKeyboard(focusView: View) {
        Log.d(logTag, "hideKeyboard()")
        inputMethodManager
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * ソフトウェアキーボードを表示する。
     * @param focusView フォーカスを当ててキーボードを表示させるビュー
     */
    fun showKeyboard(focusView: View) {
        Log.d(logTag, "showKeyboard()")
        inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * キーボードの表示状態の変更を監視するためのリスナーインターフェース。
     */
    fun interface KeyboardStateListener {
        /**
         * キーボードの表示状態が変更された時に呼び出される。
         * @param isVisible キーボードが表示されている場合はtrue
         */
        fun onVisibilityChanged(isVisible: Boolean)
    }

    /**
     * 指定されたFragmentのライフサイクルに合わせて、キーボードの状態変化リスナーを登録する。
     * リスナーはViewLifeCycleの`onDestroy`で解除される。
     * @param fragment 監視のライフサイクルオーナーとなるFragment
     * @param listener 状態変化を通知するリスナー
     */
    fun registerKeyboardStateListener(
        fragment: Fragment,
        listener: KeyboardStateListener
    ) {
        val viewLifecycleOwner = fragment.viewLifecycleOwner
        val rootView = requireNotNull(fragment.view)

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            // このObserverは、viewLifecycleOwnerが破棄されるまでメモリ上に存在する

            val insetsListener = OnApplyWindowInsetsListener { _, insets ->
                val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                Log.d(logTag, "isKeyboardVisible = $isKeyboardVisible")
                listener.onVisibilityChanged(isKeyboardVisible)
                insets
            }

            override fun onCreate(owner: LifecycleOwner) {
                ViewCompat.setOnApplyWindowInsetsListener(rootView, insetsListener)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
            }
        })
    }
}
