package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

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

public class DiaryDeleteConfirmationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + DiaryDeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_DELETE_DIARY_DATE = "DeleteDiaryDate" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_diary_delete_confirmation_title);
        LocalDate date =
                DiaryDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteDiaryDate();
        DateConverter dateConverter = new DateConverter();
        String strDate = dateConverter.toStringLocalDate(date);
        String message = strDate + getString(R.string.dialog_diary_delete_confirmation_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_diary_delete_confirmation_yes, new DialogButtonClickListener());
        builder.setNegativeButton(R.string.dialog_diary_delete_confirmation_no, new DialogButtonClickListener());
        return builder.create();
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    NavController navController =
                            NavHostFragment
                                    .findNavController(DiaryDeleteConfirmationDialogFragment.this);
                    NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                    if (navBackStackEntry == null) {
                        return;
                    }
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    LocalDate deleteDiaryDate =
                            DiaryDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments())
                                    .getDeleteDiaryDate();
                    savedStateHandle.set(KEY_DELETE_DIARY_DATE, deleteDiaryDate);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // 処理なし
                    break;
            }
        }
    }
}
