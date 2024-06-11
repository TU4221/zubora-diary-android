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
import com.websarva.wings.android.zuboradiary.ui.diary.editdiary.UpdateExistingDiaryDialogFragmentArgs;

public class UpdateExistingDiaryDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + UpdateExistingDiaryDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    private String updateDiaryDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_update_diary_dialog_title);

        this.updateDiaryDate =
                UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getUpdateDiaryDate();
        String message = this.updateDiaryDate + getString(R.string.edit_diary_update_diary_dialog_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_diary_update_diary_dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavController navController =
                        NavHostFragment
                                .findNavController(UpdateExistingDiaryDialogFragment.this);
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle.set(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
            }
        });
        builder.setNegativeButton(R.string.edit_diary_update_diary_dialog_btn_ng, null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
