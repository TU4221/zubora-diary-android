package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.app.AlertDialog;
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

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateConverter;

import java.time.LocalDate;

public class UpdateExistingDiaryDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + UpdateExistingDiaryDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    public static final String KEY_UPDATE_TYPE = "UpdateType" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_update_Existing_diary_title);

        LocalDate updateDiaryDate =
                UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getUpdateDiaryDate();
        String stringUpdateDiaryDate = DateConverter.toStringLocalDate(updateDiaryDate);
        String message = stringUpdateDiaryDate + getString(R.string.dialog_update_Existing_diary_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_update_Existing_diary_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavController navController =
                        NavHostFragment
                                .findNavController(UpdateExistingDiaryDialogFragment.this);
                NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                if (navBackStackEntry != null) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    savedStateHandle.set(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
                    int updateType =
                            UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments())
                                    .getUpdateType();
                    savedStateHandle.set(KEY_UPDATE_TYPE, updateType);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_update_Existing_diary_no, null);
        return builder.create();
    }
}
