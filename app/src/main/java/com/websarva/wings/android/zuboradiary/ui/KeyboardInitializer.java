package com.websarva.wings.android.zuboradiary.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardInitializer {

    public void hide(Activity activity, View focusView) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager
                .hideSoftInputFromWindow(
                        focusView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
    }

    public void show(Activity activity, View focusView) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager
                .showSoftInput(
                focusView,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
                );
    }
}
