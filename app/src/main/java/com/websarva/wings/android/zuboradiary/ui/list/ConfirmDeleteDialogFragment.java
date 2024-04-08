package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.websarva.wings.android.zuboradiary.R;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.list_delete_confirm_dialog_title);

        String message
                = requireArguments().getString("DeleteDate") + getString(R.string.list_delete_confirm_dialog_message);

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
                    Bundle result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        result = requireArguments().deepCopy();
                    }
                    getParentFragmentManager().setFragmentResult(
                            "ToListFragment_ConfirmDeleteDialogFragmentRequestKey",
                            result
                    );

                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //処理なし。
                    break;
            }
        }
    }
}
