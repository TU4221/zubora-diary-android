package com.websarva.wings.android.zuboradiary.ui.keyboard

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

internal class KeyboardManager(
    context: Context
) {

    private val inputMethodManager =
        context.applicationContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun hideKeyboard(focusView: View) {
        Log.d(logTag, "hideKeyboard()")
        inputMethodManager
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboard(focusView: View) {
        Log.d(logTag, "showKeyboard()")
        inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }

    fun interface KeyboardStateListener {
        fun onVisibilityChanged(isVisible: Boolean)
    }

    fun registerKeyboardStateListener(
        fragment: Fragment,
        listener: KeyboardStateListener
    ) {
        val viewLifecycleOwner = fragment.viewLifecycleOwner
        val rootView = fragment.view ?: return

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
