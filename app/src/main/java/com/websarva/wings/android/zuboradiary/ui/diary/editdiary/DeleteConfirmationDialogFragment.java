package com.websarva.wings.android.zuboradiary.ui.diary.editdiary;

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
import com.websarva.wings.android.zuboradiary.ui.diary.editdiary.DeleteConfirmationDialogFragmentArgs;

public class DeleteConfirmationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + DeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_DELETE_ITEM_NUMBER = "DeleteItemNumber" + fromClassName;
    private int deleteItemNumber;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        this.deleteItemNumber =
                DeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteItemNumber();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_delete_confirm_dialog_title);
        String message = getString(R.string.edit_diary_delete_confirm_dialog_message_item) + deleteItemNumber + getString(R.string.edit_diary_delete_confirm_dialog_message);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_diary_delete_confirm_dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                NavController navController =
                        NavHostFragment
                                .findNavController(DeleteConfirmationDialogFragment.this);
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle
                        .set(KEY_DELETE_ITEM_NUMBER, DeleteConfirmationDialogFragment.this.deleteItemNumber);
            }
        });
        builder.setNegativeButton(R.string.edit_diary_delete_confirm_dialog_btn_ng, null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
