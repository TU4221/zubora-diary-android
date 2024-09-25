package com.websarva.wings.android.zuboradiary.ui;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.websarva.wings.android.zuboradiary.R;

import java.util.Arrays;
import java.util.Objects;

public class TextInputSetup {

    private final Activity activity;

    public TextInputSetup(Activity activity) {
        Objects.requireNonNull(activity);

        this.activity = activity;
    }

    @NonNull
    public EditText getTextInputEditTextNonNull(TextInputLayout textInputLayout) {
        Objects.requireNonNull(textInputLayout);

        EditText editText = textInputLayout.getEditText();
        Objects.requireNonNull(editText);
        return editText;
    }

    private void hideKeyboard(View view) {
        KeyboardInitializer keyboardInitializer = new KeyboardInitializer(activity);
        keyboardInitializer.hide(view);
    }

    public void setUpScrollable(TextInputLayout... textInputLayouts) {
        Objects.requireNonNull(textInputLayouts);
        Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

        Arrays.stream(textInputLayouts).forEach(x -> {
            EditText editText = getTextInputEditTextNonNull(x);
            editText.setOnFocusChangeListener(
                    new EditTextScrollableOnFocusChangeListener()
            );
        });
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


    public void setUpKeyboardCloseOnEnter(TextInputLayout... textInputLayouts) {
        Objects.requireNonNull(textInputLayouts);
        Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

        // 入力欄エンターキー押下時の処理。
        Arrays.stream(textInputLayouts).forEach(x -> {
            EditText editText = getTextInputEditTextNonNull(x);
            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO:vはFocusView
                    Objects.requireNonNull(v);
                    Objects.requireNonNull(event);
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                    if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

                    // HACK:InputTypeの値が何故か1ズレている。(公式のリファレンスでもズレあり。)(setとgetを駆使してLogで確認確認済み)
                    if (editText.getInputType() == (TYPE_TEXT_FLAG_MULTI_LINE + 1)) return false;

                    hideKeyboard(v);
                    editText.clearFocus(); // 自テキストフォーカスクリア。

                    return false; // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
                }
            });
        });
    }

    public void setUpFocusClearOnClickBackground(View background, TextInputLayout... textInputLayouts) {
        Objects.requireNonNull(background);
        Objects.requireNonNull(textInputLayouts);
        Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                hideKeyboard(v);
                Arrays.stream(textInputLayouts).forEach(x -> {
                    EditText editText = getTextInputEditTextNonNull(x);
                    editText.clearFocus();
                });
            }
        });
    }

    /**
     * クリアボタンを設定するListenerを作成。BaseFragment#addTransitionListenerに渡すこと。<br>
     * 上記方法をとる理由は下記不具合が発生する為。<br><br>
     * xmlファイル、又はレイアウト前の処理コードにてTextInputLayoutのEndIconを設定した時、<br>
     * FragmentのTransitionを処理させると、hintラベルがずれる不具合が発生。
    * */
    // HACK:xmlファイル、又はレイアウト前のコードにてTextInputLayoutのEndIconを設定した時、FragmentのTransitionを処理させると、
    //      hintラベルがずれる不具合が発生。これはTransitionとラベルのアニメーションが干渉している事が原因らしい。
    //      対策としてTransition完了後にEndIconを設定するようにTransitionListenerを作成し対象Transitionに設定。
    public ClearButtonSetUpTransitionListener createClearButtonSetupTransitionListener(TextInputLayout... textInputLayouts) {
        Objects.requireNonNull(textInputLayouts);
        Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

        return new ClearButtonSetUpTransitionListener(textInputLayouts);
    }

    public static class ClearButtonSetUpTransitionListener implements Transition.TransitionListener {

        TextInputLayout[] textInputLayouts;
        private ClearButtonSetUpTransitionListener(TextInputLayout... textInputLayouts) {
            Objects.requireNonNull(textInputLayouts);
            Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

            this.textInputLayouts = textInputLayouts;
        }

        @Override
        public void onTransitionStart(Transition transition) {
            // 処理なし
        }

        @Override
        public void onTransitionEnd(Transition transition) {
            Objects.requireNonNull(transition);

            setUpClearButton(textInputLayouts);
        }

        @Override
        public void onTransitionCancel(Transition transition) {
            // 処理なし
        }

        @Override
        public void onTransitionPause(Transition transition) {
            // 処理なし
        }

        @Override
        public void onTransitionResume(Transition transition) {
            // 処理なし
        }

        // HACK:クリアボタンはEND_ICON_CLEAR_BUTTONで容易にクリアボタンを実装できるが、
        //      下記の理由でにEND_ICON_CUSTOMを通して設定している。
        //      ・コードからEditTextの文字列を設定してもクリアボタンが表示されない。
        //      ・FragmentのTransition完了後にEndIconを設定しても一度画面からEditTextのTextを編集すると、
        //        フォーカスしない限りクリアボタンが表示されなくなる。
        private void setUpClearButton(TextInputLayout... textInputLayouts) {
            Objects.requireNonNull(textInputLayouts);
            Arrays.stream(textInputLayouts).forEach(Objects::requireNonNull);

            Arrays.stream(textInputLayouts).forEach(x -> {
                x.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                x.setEndIconDrawable(R.drawable.ic_cancel_24px);
                TextInputEditText textInputEditText = (TextInputEditText) x.getEditText();
                Objects.requireNonNull(textInputEditText);
                Editable text = textInputEditText.getText();
                Objects.requireNonNull(text);
                boolean isVisible = !text.toString().isEmpty();
                x.setEndIconVisible(isVisible);
                x.setEndIconOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textInputEditText.setText("");
                    }
                });
                ClearButtonVisibleSwitchingTextWatcher textWatcher = new ClearButtonVisibleSwitchingTextWatcher(x);
                textInputEditText.addTextChangedListener(textWatcher);
            });
        }

        private static class ClearButtonVisibleSwitchingTextWatcher implements TextWatcher {

            private final TextInputLayout textInputLayout;

            ClearButtonVisibleSwitchingTextWatcher(TextInputLayout textInputLayout) {
                this.textInputLayout = textInputLayout;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 処理なし
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isVisible = !s.toString().isEmpty();
                textInputLayout.setEndIconVisible(isVisible);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 処理なし
            }
        }
    }
}
