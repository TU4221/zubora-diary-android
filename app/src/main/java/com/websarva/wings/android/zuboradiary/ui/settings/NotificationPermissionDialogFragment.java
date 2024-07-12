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

import com.websarva.wings.android.zuboradiary.ui.MessageDialogFragmentArgs;

public class NotificationPermissionDialogFragment extends DialogFragment {
    private static final String FROM_CLASS_NAME = "From" + DayOfWeekPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + FROM_CLASS_NAME;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Navigation設定
        NavController navController = NavHostFragment.findNavController(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("権限が必要です");
        builder.setMessage(
                "このアプリを正常に動作させるためには通知権限が必要です。設定画面で権限を有効にしてください。");
        builder.setPositiveButton(
                "設定画面を開く", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SavedStateHandle savedStateHandle =
                                navController.getPreviousBackStackEntry().getSavedStateHandle();
                        savedStateHandle.set(KEY_SELECTED_BUTTON, which);
                        navController.navigateUp();
                    }
                });
        builder.setNegativeButton(
                "いいえ", null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        setCancelable(true);

        return dialog;
    }
}
