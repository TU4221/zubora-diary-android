package com.websarva.wings.android.zuboradiary.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

import dagger.internal.Preconditions;

public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setUpDialogCancelFunction();

        ThemeColor themeColor = getActivityThemeColor();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        return createDialogView(themeColorInflater, container, savedInstanceState);
    }

    private void setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        if (!isCancelableOtherThanPressingButton()) {
            this.setCancelable(false);
        }
    }

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * */
    protected abstract boolean isCancelableOtherThanPressingButton();

    protected final ThemeColor getActivityThemeColor() {
        Activity activity = requireActivity();
        MainActivity mainActivity;
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        } else {
            throw new ClassCastException();
        }
        return mainActivity.requireDialogThemeColor();
    }

    // ThemeColorに合わせたインフレーター作成
    protected final LayoutInflater createThemeColorInflater(LayoutInflater inflater, ThemeColor themeColor) {
        Preconditions.checkNotNull(inflater);
        Preconditions.checkNotNull(themeColor);

        int themeResId = themeColor.getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireActivity(), themeResId);
        return inflater.cloneInContext(contextWithTheme);
    }

    protected abstract View createDialogView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected final class PositiveButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Preconditions.checkNotNull(v);

            handlePositiveButton(v);
            closeDialog();
        }
    }

    protected final class NegativeButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Preconditions.checkNotNull(v);

            handleNegativeButton(v);
            closeDialog();
        }
    }

    private void closeDialog() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }

    protected abstract void handlePositiveButton(@NonNull View v);

    protected abstract void handleNegativeButton(@NonNull View v);

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

    protected abstract void handleCancel(@NonNull DialogInterface dialog);

    @Override
    public final void dismiss() {
        handleDismiss();
        super.dismiss();
    }
    
    protected abstract void handleDismiss();
}
