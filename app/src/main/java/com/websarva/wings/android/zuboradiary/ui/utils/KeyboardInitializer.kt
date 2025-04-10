package com.websarva.wings.android.zuboradiary.ui.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.websarva.wings.android.zuboradiary.createLogTag

internal class KeyboardInitializer(activity: Activity) {

    private val logTag = createLogTag()

    private val inputMethodManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun hide(focusView: View) {
        Log.d(logTag, "hide()")
        inputMethodManager
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun show(focusView: View) {
        Log.d(logTag, "show()")
        inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }
}
