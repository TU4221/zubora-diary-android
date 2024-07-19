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
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_exists_diary_dialog_title);

        String loadDiaryDate =
                LoadExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getLoadDiaryDate();
        String message = loadDiaryDate + getString(R.string.edit_diary_exists_diary_dialog_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_diary_exists_diary_dialog_btn_ok, new custumOnClickListener());
        builder.setNegativeButton(R.string.edit_diary_exists_diary_dialog_btn_ng, new custumOnClickListener());
        return builder.create();
    }

    private class custumOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            NavController navController =
                    NavHostFragment
                            .findNavController(LoadExistingDiaryDialogFragment.this);
            SavedStateHandle savedStateHandle =
                    navController.getPreviousBackStackEntry().getSavedStateHandle();
            savedStateHandle
                    .set(
                            KEY_SELECTED_BUTTON,
                            which
                    );
        }
    }
}
