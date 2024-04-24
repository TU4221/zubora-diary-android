package com.websarva.wings.android.zuboradiary;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {
    private static Activity activity;

    public static void setInputMethodManager(Activity a) {
        activity = a;

    }
    public static void hide(View focusView) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager
                .hideSoftInputFromWindow(
                        focusView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
    }
    /*public static void show(View focusView) {
        inputMethodManager
                .showSoftInput(
                focusView,
                InputMethodManager.SHOW_IMPLICIT
                );
    }*/
    public static void show() {

        /*inputMethodManager
                .toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_NOT_ALWAYS
                );*/

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }
}
