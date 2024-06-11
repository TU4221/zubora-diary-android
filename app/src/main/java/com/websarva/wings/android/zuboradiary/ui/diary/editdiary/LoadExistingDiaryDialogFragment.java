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
import com.websarva.wings.android.zuboradiary.ui.diary.editdiary.LoadExistingDiaryDialogFragmentArgs;

public class LoadExistingDiaryDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + LoadExistingDiaryDialogFragment.class.getName();
    public static final String KEY_LOAD_DIARY_DATE = "LoadDiaryDate" + fromClassName;
    private String loadDiaryDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_exists_diary_dialog_title);

        this.loadDiaryDate =
                LoadExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getLoadDiaryDate();
        String message = this.loadDiaryDate + getString(R.string.edit_diary_exists_diary_dialog_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_diary_exists_diary_dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavController navController =
                        NavHostFragment
                                .findNavController(LoadExistingDiaryDialogFragment.this);
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle
                        .set(
                                KEY_LOAD_DIARY_DATE,
                                LoadExistingDiaryDialogFragment.this.loadDiaryDate
                        );
            }
        });
        builder.setNegativeButton(R.string.edit_diary_exists_diary_dialog_btn_ng, null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
