package com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.websarva.wings.android.zuboradiary.R;

public class ConfirmDeleteDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_select_item_title_confirm_delete_dialog_title);

        String message =
                getString(R.string.edit_diary_select_item_title_confirm_delete_dialog_first_message)
                + requireArguments().getString("SelectedItemTitle")
                + getString(
                        R.string.edit_diary_select_item_title_confirm_delete_dialog_second_message);

        builder.setMessage(message);
        builder.setPositiveButton(
                R.string.edit_diary_select_item_title_confirm_delete_dialog_btn_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repairFragmentResult(DialogInterface.BUTTON_POSITIVE);
                    }
                }
        );
        builder.setNegativeButton(
                R.string.edit_diary_select_item_title_confirm_delete_dialog_btn_ng,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repairFragmentResult(DialogInterface.BUTTON_NEGATIVE);
                    }
                }
        );
        AlertDialog dialog = builder.create();

        // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
        //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
        //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
        //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
        //      UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセルする)ことができるが、
        //      これを無効にするには、AlertDialog#setCanceledOnTouchOutsideを設定する必要あり。
        //      またスマホ等の戻るボタンでもダイアログを閉じる(キャンセルする)ことは可能だが、
        //      これを無効にするには、DialogFragment#setCancelableを設定する必要あり。
        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        return dialog;
    }

    @Override
    public void onCancel (DialogInterface dialog) {
        Log.d("20240321", "cancel");
        super.onCancel(dialog);
        repairFragmentResult(DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    public void onDismiss (DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("20240321", "dismiss");
    }

    private void repairFragmentResult(int status) {
        Bundle result = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            result = requireArguments().deepCopy();
        }

        result.putInt("SelectedButtonResult", status);

        getParentFragmentManager().setFragmentResult(
                "ToEditDiarySelectItemTitleFragment_ConfirmDeleteDialogFragmentRequestKey",
                result
        );
    }
}
