package com.websarva.wings.android.zuboradiary.ui;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.StartYearMonthPickerDialogFragmentArgs;

import java.util.Objects;

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
