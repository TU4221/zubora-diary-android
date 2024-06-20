package com.websarva.wings.android.zuboradiary.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.websarva.wings.android.zuboradiary.R;

public class MessageDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String title =
                MessageDialogFragmentArgs.fromBundle(requireArguments()).getTitle();
        builder.setTitle(title);

        String message =
                MessageDialogFragmentArgs.fromBundle(requireArguments()).getMessage();
        builder.setMessage(message);
        builder.setPositiveButton(
                R.string.edit_diary_select_item_title_confirm_delete_dialog_btn_ok, null);

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
}
