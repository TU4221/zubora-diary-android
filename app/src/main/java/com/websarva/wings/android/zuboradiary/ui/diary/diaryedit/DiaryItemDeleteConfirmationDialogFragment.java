package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

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

import com.websarva.wings.android.zuboradiary.R;

public class DiaryItemDeleteConfirmationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + DiaryItemDeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_DELETE_ITEM_NUMBER = "DeleteItemNumber" + fromClassName;
    private int deleteItemNumber;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        this.deleteItemNumber =
                DiaryItemDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteItemNumber();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_diary_item_delete_confirmation_title);
        String message = getString(R.string.dialog_diary_item_delete_confirmation_first_message) + deleteItemNumber + getString(R.string.dialog_diary_item_delete_confirmation_second_message);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_diary_item_delete_confirmation_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                NavController navController =
                        NavHostFragment
                                .findNavController(DiaryItemDeleteConfirmationDialogFragment.this);
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle
                        .set(KEY_DELETE_ITEM_NUMBER, DiaryItemDeleteConfirmationDialogFragment.this.deleteItemNumber);
            }
        });
        builder.setNegativeButton(R.string.dialog_diary_item_delete_confirmation_no, null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
