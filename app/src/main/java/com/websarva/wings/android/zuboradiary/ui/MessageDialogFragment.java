package com.websarva.wings.android.zuboradiary.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.websarva.wings.android.zuboradiary.R;

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
    public boolean isCancelableOtherThanPressingButton() {
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
