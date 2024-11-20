package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.util.Objects;

public class AllDiariesDeleteConfirmationDialogFragment extends BaseAlertDialogFragment {

    private static final String FROM_CLASS_NAME =
            "From" + AllDiariesDeleteConfirmationDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + FROM_CLASS_NAME;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_all_diaries_delete_confirmation_title);
    }

    @Override
    protected String createMessage() {
        return getString(R.string.dialog_all_diaries_delete_confirmation_message);
    }

    @Override
    protected void handleOnClickPositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnClickNegativeButton(@NonNull DialogInterface dialog, int which) {
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
