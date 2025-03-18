package com.websarva.wings.android.zuboradiary.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

internal class KeyboardInitializer(activity: Activity) {
    private val inputMethodManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun hide(focusView: View) {
        Log.d(javaClass.simpleName, "hide()")
        inputMethodManager
            .hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun show(focusView: View) {
        Log.d(javaClass.simpleName, "show()")
        inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }
}
