package com.websarva.wings.android.zuboradiary;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {
    private static InputMethodManager inputMethodManager;

    public static void setInputMethodManager(Activity activity) {
        inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    public static void hide(View focusView) {
        inputMethodManager
                .hideSoftInputFromWindow(
                        focusView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
    }
}
