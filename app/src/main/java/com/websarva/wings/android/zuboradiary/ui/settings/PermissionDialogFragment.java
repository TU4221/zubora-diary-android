package com.websarva.wings.android.zuboradiary.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class PermissionDialogFragment extends BaseAlertDialogFragment {

    private static final String FROM_CLASS_NAME = "From" + DayOfWeekPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + FROM_CLASS_NAME;

    @Override
    protected String createTitle() {
        return "権限が必要です";
    }

    @Override
    protected String createMessage() {
        String firstMessage = "この機能を正常に動作させるためには";
        String secondMessage = PermissionDialogFragmentArgs.fromBundle(requireArguments()).getPermissionName();
        String thirdMessage = "権限が必要です。設定画面で権限を有効にしてください。";
        return firstMessage + secondMessage + thirdMessage;
    }

    @Override
    protected void handlePositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleNegativeButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    protected void handleCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleDismiss() {
        // 処理なし
    }
}
