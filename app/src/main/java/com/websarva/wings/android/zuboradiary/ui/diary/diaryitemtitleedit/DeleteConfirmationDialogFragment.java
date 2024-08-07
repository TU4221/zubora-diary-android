package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

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

public class DeleteConfirmationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + DeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    public static final String KEY_DELETE_LIST_ITEM_POSITION = "DeleteItemPosition" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_diary_select_item_title_confirm_delete_dialog_title);

        String deleteItemTitle =
                DeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteItemTitle();
        String message = getString(R.string.edit_diary_select_item_title_confirm_delete_dialog_first_message) + deleteItemTitle + getString(
                        R.string.edit_diary_select_item_title_confirm_delete_dialog_second_message);

        builder.setMessage(message);
        builder.setPositiveButton(
                R.string.edit_diary_select_item_title_confirm_delete_dialog_btn_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        processResults(DialogInterface.BUTTON_POSITIVE);
                    }
                }
        );
        builder.setNegativeButton(
                R.string.edit_diary_select_item_title_confirm_delete_dialog_btn_ng,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        processResults(DialogInterface.BUTTON_NEGATIVE);
                    }
                }
        );
        AlertDialog dialog = builder.create();

        // TODO:コメントが理解できないので動作を確認してコメント修正
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

    // ダイアログ枠外タッチ、popBackStack時に処理
    @Override
    public void onCancel (DialogInterface dialog) {
        super.onCancel(dialog);
        processResults(DialogInterface.BUTTON_NEGATIVE);
    }

    // ダイアログ消失時に処理
    @Override
    public void onDismiss (DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private void processResults(int status) {
        NavController navController = NavHostFragment.findNavController(this);
        SavedStateHandle savedStateHandle =
                navController.getPreviousBackStackEntry().getSavedStateHandle();
        savedStateHandle.set(KEY_SELECTED_BUTTON, status);
        int deleteListItemPosition =
                DeleteConfirmationDialogFragmentArgs.fromBundle(getArguments()).getDeleteListItemPosition();
        savedStateHandle.set(KEY_DELETE_LIST_ITEM_POSITION, deleteListItemPosition);
    }
}
