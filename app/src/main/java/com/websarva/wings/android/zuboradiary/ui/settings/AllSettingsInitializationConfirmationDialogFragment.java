package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class AllSettingsInitializationConfirmationDialogFragment extends BaseAlertDialogFragment {

    private static final String FROM_CLASS_NAME =
            "From" + AllSettingsInitializationConfirmationDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + FROM_CLASS_NAME;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_all_settings_initialization_confirmation_title);
    }

    @Override
    protected String createMessage() {
        return getString(R.string.dialog_all_settings_initialization_confirmation_message);
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
