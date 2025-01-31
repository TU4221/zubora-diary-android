package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class AllDiariesDeleteDialogFragment extends BaseAlertDialogFragment {

    private static final String FROM_CLASS_NAME =
            "From" + AllDiariesDeleteDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + FROM_CLASS_NAME;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_all_diaries_delete_title);
    }

    @Override
    protected String createMessage() {
        return getString(R.string.dialog_all_diaries_delete_message);
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnNegativeButtonClick(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }
}
