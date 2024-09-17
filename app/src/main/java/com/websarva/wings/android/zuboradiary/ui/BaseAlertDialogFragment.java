package com.websarva.wings.android.zuboradiary.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import dagger.internal.Preconditions;

public abstract class BaseAlertDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public final Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        MainActivity mainActivity;
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        } else {
            throw new ClassCastException();
        }
        int themeResId = mainActivity.requireDialogThemeColor().getAlertDialogThemeResId();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), themeResId);

        String title = createTitle();
        Preconditions.checkNotNull(title);
        builder.setTitle(title);

        String message = createMessage();
        Preconditions.checkNotNull(message);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.dialog_base_alert_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Preconditions.checkNotNull(dialog);

                handlePositiveButton(dialog, which);
            }
        });

        builder.setNegativeButton(R.string.dialog_base_alert_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Preconditions.checkNotNull(dialog);

                handleNegativeButton(dialog, which);
            }
        });

        AlertDialog alertDialog = builder.create();

        // MEMO:UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセルする)ことができるが、
        //      これを無効にするには、AlertDialog#setCanceledOnTouchOutsideを設定する必要あり。
        //      またスマホ等の戻るボタンでもダイアログを閉じる(キャンセルする)ことは可能だが、
        //      これを無効にするには、DialogFragment#setCancelableを設定する必要あり。
        if (!isCancelable()) {
            alertDialog.setCanceledOnTouchOutside(false);
            this.setCancelable(false);
        }

        return alertDialog;
    }

    protected final void setResult(String resultKey, Object result) {
        Preconditions.checkNotNull(resultKey);
        Preconditions.checkNotNull(result);

        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Preconditions.checkNotNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();

        savedStateHandle.set(resultKey, result);
    }

    // ダイアログ枠外タッチ、popBackStack時に処理
    // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
    //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
    //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
    //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
    @Override
    public final void onCancel(@NonNull DialogInterface dialog) {
        handleCancel(dialog);
        super.onCancel(dialog);
    }

    @Override
    public final void dismiss() {
        handleDismiss();
        super.dismiss();
    }

    protected abstract String createTitle();

    protected abstract String createMessage();

    protected abstract void handlePositiveButton(@NonNull DialogInterface dialog, int which);

    protected abstract void handleNegativeButton(@NonNull DialogInterface dialog, int which);

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * */
    public abstract boolean isCancelable();

    protected abstract void handleCancel(@NonNull DialogInterface dialog);

    protected abstract void handleDismiss();
}
