package com.websarva.wings.android.zuboradiary.ui.keyboard

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class KeyboardManager(activity: Activity) {

    private val logTag = createLogTag()

    private val inputMethodManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun hideKeyboard(focusView: View) {
        Log.d(logTag, "hideKeyboard()")
        inputMethodManager
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboard(focusView: View) {
        Log.d(logTag, "showKeyboard()")
        inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }
}
