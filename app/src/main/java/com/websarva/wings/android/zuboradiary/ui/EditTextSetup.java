package com.websarva.wings.android.zuboradiary.ui;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.websarva.wings.android.zuboradiary.R;

import java.util.Arrays;
import java.util.Objects;

public class EditTextSetup {

    private final Activity activity;

    public EditTextSetup(Activity activity) {
        Objects.requireNonNull(activity);

        this.activity = activity;
    }

    protected void hideKeyboard(View view) {
        KeyboardInitializer keyboardInitializer = new KeyboardInitializer(activity);
        keyboardInitializer.hide(view);
    }

    public void setUpScrollable(EditText... editTexts) {
        Objects.requireNonNull(editTexts);
        Arrays.stream(editTexts).forEach(Objects::requireNonNull);

        Arrays.stream(editTexts).forEach(this::setUpScrollable);
    }

    protected void setUpScrollable(EditText editText) {
        Objects.requireNonNull(editText);

        editText.setOnFocusChangeListener(new EditTextScrollableOnFocusChangeListener());
    }

    private static class EditTextScrollableOnFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Objects.requireNonNull(v);

            if (hasFocus) {
                setUpEditTextScrollable(v);
            } else {
                resetEditTextScrollable(v);
            }
        }

        private void setUpEditTextScrollable(View focusedView) {
            Objects.requireNonNull(focusedView);

            if (focusedView instanceof EditText) {
                EditText a = (EditText) focusedView;
                if (a.getMinLines() > 1) {
                    focusedView.setOnTouchListener(new ScrollableTextOnTouchListener());
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        private void resetEditTextScrollable(View focusedView) {
            Objects.requireNonNull(focusedView);

            if (focusedView instanceof EditText) {
                EditText a = (EditText) focusedView;
                if (a.getMinLines() > 1) {
                    focusedView.setOnTouchListener(null);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        private static class ScrollableTextOnTouchListener implements View.OnTouchListener {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!v.canScrollVertically(1) && !v.canScrollVertically(-1)) return false;

                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        }
    }

    public void setUpKeyboardCloseOnEnter(EditText... editTexts) {
        Objects.requireNonNull(editTexts);
        Arrays.stream(editTexts).forEach(Objects::requireNonNull);

        Arrays.stream(editTexts).forEach(this::setUpKeyboardCloseOnEnter);
    }

    protected void setUpKeyboardCloseOnEnter(EditText editText) {
        Objects.requireNonNull(editText);

        editText.setOnKeyListener(new KeyboardCloseOnEnterListener());
    }

    private class KeyboardCloseOnEnterListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Objects.requireNonNull(v);
            Objects.requireNonNull(event);
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

            EditText editText = (EditText) v;
            // HACK:InputTypeの値が何故か1ズレている。(公式のリファレンスでもズレあり。)(setとgetを駆使してLogで確認確認済み)
            if (editText.getInputType() == (TYPE_TEXT_FLAG_MULTI_LINE + 1)) return false;

            hideKeyboard(v);
            editText.clearFocus();

            return false; // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
        }
    }

    public void setUpFocusClearOnClickBackground(View background, EditText... editTexts) {
        Objects.requireNonNull(background);
        Objects.requireNonNull(editTexts);
        Arrays.stream(editTexts).forEach(Objects::requireNonNull);

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                hideKeyboard(v);
                Arrays.stream(editTexts).forEach(EditText::clearFocus);
            }
        });
    }

    public void setUpClearButton(EditText editText, ImageButton clearButton) {
        Objects.requireNonNull(editText);
        Objects.requireNonNull(clearButton);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 処理なし
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Objects.requireNonNull(s);

                boolean isVisible = !s.toString().isEmpty();
                int visibility;
                if (isVisible) {
                    visibility = View.VISIBLE;
                } else {
                    visibility = View.INVISIBLE;
                }
                clearButton.setVisibility(visibility);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 処理なし
            }
        });

        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
    }
}
