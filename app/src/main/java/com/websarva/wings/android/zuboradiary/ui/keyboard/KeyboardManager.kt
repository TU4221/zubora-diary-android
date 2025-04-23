package com.websarva.wings.android.zuboradiary.ui.keyboard

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class KeyboardManager {

    private val logTag = createLogTag()

    private fun getInputMethodManager(view: View): InputMethodManager {
        return view.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    fun hideKeyboard(focusView: View) {
        Log.d(logTag, "hideKeyboard()")
        getInputMethodManager(focusView)
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboard(focusView: View) {
        Log.d(logTag, "showKeyboard()")
        getInputMethodManager(focusView).showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }

    fun interface KeyboardStateListener {
        fun onVisibilityChanged(isShowed: Boolean)
    }

    fun registerKeyBoredStateListener(
        fragment: Fragment,
        listener: KeyboardStateListener
    ) {
        val rootView = fragment.view ?: throw IllegalArgumentException()

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            Log.d(logTag, "isKeyboardVisible = $isKeyboardVisible")

            listener.onVisibilityChanged(isKeyboardVisible)

            return@setOnApplyWindowInsetsListener insets
        }
    }
}
