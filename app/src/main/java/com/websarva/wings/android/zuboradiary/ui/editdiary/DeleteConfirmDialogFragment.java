package com.websarva.wings.android.zuboradiary.ui.editdiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.websarva.wings.android.zuboradiary.R;

public class DeleteConfirmDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int deleteItemNo = requireArguments().getInt("DeleteItemNo");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_delete_confirm_dialog_title);
        String message = getString(R.string.edit_diary_delete_confirm_dialog_message_item) + deleteItemNo + getString(R.string.edit_diary_delete_confirm_dialog_message);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_diary_delete_confirm_dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Bundle result = new Bundle();
                result.putInt("DeleteItemNo", deleteItemNo);
                getParentFragmentManager().setFragmentResult("DeleteConfirmDialogRequestKey", result);
            }
        });
        builder.setNegativeButton(R.string.edit_diary_delete_confirm_dialog_btn_ng, null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
