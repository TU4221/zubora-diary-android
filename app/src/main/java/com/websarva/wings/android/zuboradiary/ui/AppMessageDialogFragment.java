package com.websarva.wings.android.zuboradiary.ui;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.websarva.wings.android.zuboradiary.data.AppMessage;

import java.util.Objects;

public class AppMessageDialogFragment extends BaseAlertDialogFragment {

    @Override
    protected String createTitle() {
        AppMessage appMessage =
                AppMessageDialogFragmentArgs.fromBundle(requireArguments()).getAppMessage();
        Objects.requireNonNull(appMessage);

        return appMessage.getDialogTitle(requireContext());
    }

    @Override
    protected String createMessage() {
        AppMessage appMessage =
                AppMessageDialogFragmentArgs.fromBundle(requireArguments()).getAppMessage();
        Objects.requireNonNull(appMessage);

        return appMessage.getDialogMessage(requireContext());
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which) {
        // 処理なし
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

    @Override
    protected void customizeDialog(MaterialAlertDialogBuilder builder) {
        super.customizeDialog(builder);
        builder.setNegativeButton("", null);
    }
}
