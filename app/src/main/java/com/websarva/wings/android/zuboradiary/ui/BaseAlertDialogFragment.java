package com.websarva.wings.android.zuboradiary.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.util.HashMap;

import dagger.internal.Preconditions;

public abstract class BaseAlertDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        MainActivity mainActivity;
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        } else {
            throw new ClassCastException();
        }
        int themeResId = mainActivity.requireDialogThemeColor().getAlertDialogThemeResId();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), themeResId);
        builder.setTitle(createTitle());
        builder.setMessage(createMessage());
        builder.setPositiveButton(R.string.dialog_diary_item_delete_confirmation_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handlePositiveButton(dialog, which);
            }
        });
        builder.setNegativeButton(R.string.dialog_diary_item_delete_confirmation_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleNegativeButton(dialog, which);
            }
        });

        return builder.create();
    }

    protected void setResult(String resultKey, Object result) {
        Preconditions.checkNotNull(resultKey);
        Preconditions.checkNotNull(result);

        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Preconditions.checkNotNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();

        savedStateHandle.set(resultKey, result);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        handleCancel(dialog);
    }

    @Override
    public void dismiss() {
        handleDismiss();
    }

    protected abstract String createTitle();
    protected abstract String createMessage();
    protected abstract void handlePositiveButton(@NonNull DialogInterface dialog, int which);
    protected abstract void handleNegativeButton(@NonNull DialogInterface dialog, int which);
    protected abstract void handleCancel(@NonNull DialogInterface dialog);
    protected abstract void handleDismiss();
}
