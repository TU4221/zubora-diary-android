package com.websarva.wings.android.zuboradiary.ui;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

public class MessageDialogFragment extends BaseAlertDialogFragment {

    @Override
    protected String createTitle() {
        return MessageDialogFragmentArgs.fromBundle(requireArguments()).getTitle();
    }

    @Override
    protected String createMessage() {
        return MessageDialogFragmentArgs.fromBundle(requireArguments()).getMessage();
    }

    @Override
    protected void handlePositiveButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    protected void handleNegativeButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
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
