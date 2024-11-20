package com.websarva.wings.android.zuboradiary.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.util.Objects;

public abstract class BaseAlertDialogFragment extends DialogFragment {

    protected SettingsViewModel settingsViewModel;

    @NonNull
    @Override
    public final Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        settingsViewModel = createSettingsViewModel();

        int themeResId = requireThemeColor().getAlertDialogThemeResId();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), themeResId);

        customizeDialog(builder);

        AlertDialog alertDialog = builder.create();

        setUpDialogCancelFunction(alertDialog);

        return alertDialog;
    }

    @NonNull
    private SettingsViewModel createSettingsViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        SettingsViewModel settingsViewModel = provider.get(SettingsViewModel.class);
        return Objects.requireNonNull(settingsViewModel);
    }

    @NonNull
    protected final ThemeColor requireThemeColor() {
        return settingsViewModel.loadThemeColorSettingValue();
    }

    protected void customizeDialog(MaterialAlertDialogBuilder builder) {
        Objects.requireNonNull(builder);

        String title = createTitle();
        Objects.requireNonNull(title);
        builder.setTitle(title);

        String message = createMessage();
        Objects.requireNonNull(message);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.dialog_base_alert_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Objects.requireNonNull(dialog);
                handleOnPositiveButtonClick(dialog, which);
            }
        });

        builder.setNegativeButton(R.string.dialog_base_alert_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Objects.requireNonNull(dialog);
                handleOnNegativeButtonClick(dialog, which);
            }
        });
    }

    private void setUpDialogCancelFunction(AlertDialog alertDialog) {
        // MEMO:下記機能を無効にするにはAlertDialog#setCanceledOnTouchOutside、DialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)(AlertDialog#setCanceledOnTouchOutside)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)(DialogFragment#setCancelable)
        if (!isCancelableOtherThanPressingButton()) {
            alertDialog.setCanceledOnTouchOutside(false);
            this.setCancelable(false);
        }
    }

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     * */
    protected abstract String createTitle();

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     * */
    protected abstract String createMessage();

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     * */
    protected abstract void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which);

    /**
     * BaseAlertDialogFragment.customizeDialog()で呼び出される。
     * */
    protected abstract void handleOnNegativeButtonClick(@NonNull DialogInterface dialog, int which);

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * */
    protected abstract boolean isCancelableOtherThanPressingButton();

    // ダイアログ枠外タッチ、popBackStack時に処理
    // MEMO:ダイアログフラグメントのCANCEL・DISMISS 処理について、
    //      このクラスのような、DialogFragmentにAlertDialogを作成する場合、
    //      CANCEL・DISMISSの処理内容はDialogFragmentのonCancel/onDismissをオーバーライドする必要がある。
    //      DialogFragment、AlertDialogのリスナセットメソッドを使用して処理内容を記述きても処理はされない。
    @Override
    public final void onCancel(@NonNull DialogInterface dialog) {
        handleOnCancel(dialog);
        super.onCancel(dialog);
    }

    /**
     * BaseAlertDialogFragment.onCancel()で呼び出される。
     * */
    protected abstract void handleOnCancel(@NonNull DialogInterface dialog);

    @Override
    public final void dismiss() {
        handleOnDismiss();
        super.dismiss();
    }

    /**
     * BaseAlertDialogFragment.dismiss()で呼び出される。
     * */
    protected abstract void handleOnDismiss();

    protected final void setResult(String resultKey, Object result) {
        Objects.requireNonNull(resultKey);
        Objects.requireNonNull(result);

        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();

        savedStateHandle.set(resultKey, result);
    }
}
