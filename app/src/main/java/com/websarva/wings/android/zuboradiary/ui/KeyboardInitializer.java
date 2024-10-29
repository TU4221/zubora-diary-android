package com.websarva.wings.android.zuboradiary.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

public class KeyboardInitializer {

    private final InputMethodManager inputMethodManager;

    public KeyboardInitializer(Activity activity) {
        Objects.requireNonNull(activity);

        inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hide(View focusView) {
        Objects.requireNonNull(focusView);

        inputMethodManager
                .hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void show(View focusView) {
        Objects.requireNonNull(focusView);

        inputMethodManager
                .showSoftInput(focusView, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
