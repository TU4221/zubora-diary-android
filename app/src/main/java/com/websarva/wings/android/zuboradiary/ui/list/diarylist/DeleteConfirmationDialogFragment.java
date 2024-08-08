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

public class DeleteConfirmationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + DeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_DELETE_DIARY_DATE = "DeleteDiaryDate" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.list_delete_confirm_dialog_title);
        LocalDate date =
                DeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteDiaryDate();
        String strDate = DateConverter.toStringLocalDate(date);
        String message = strDate + getString(R.string.list_delete_confirm_dialog_message);

        builder.setMessage(message);
        builder.setPositiveButton(R.string.list_delete_confirm_dialog_btn_ok, new DialogButtonClickListener());
        builder.setNegativeButton(R.string.list_delete_confirm_dialog_btn_ng, new DialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    NavController navController =
                            NavHostFragment
                                    .findNavController(DeleteConfirmationDialogFragment.this);
                    NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                    if (navBackStackEntry == null) {
                        return;
                    }
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    LocalDate deleteDiaryDate =
                            DeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments())
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
