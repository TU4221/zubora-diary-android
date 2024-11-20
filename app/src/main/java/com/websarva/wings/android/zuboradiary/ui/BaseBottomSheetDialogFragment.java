package com.websarva.wings.android.zuboradiary.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.util.Objects;

public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    protected SettingsViewModel settingsViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setUpDialogCancelFunction();

        settingsViewModel = createSettingsViewModel();

        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, requireThemeColor());
        return createDialogView(themeColorInflater, container, savedInstanceState);
    }

    private void setUpDialogCancelFunction() {
        // MEMO:下記機能を無効にするにはDialogFragment#setCancelableを設定する必要あり。
        //      ・UIに表示されているダイアログ外の部分をタッチしてダイアログを閉じる(キャンセル)
        //      ・端末の戻るボタンでダイアログを閉じる(キャンセルする)
        if (!isCancelableOtherThanPressingButton()) this.setCancelable(false);
    }

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * BaseBottomSheetDialogFragment#setUpDialogCancelFunction()で呼び出される。
     * */
    protected abstract boolean isCancelableOtherThanPressingButton();

    @NonNull
    private SettingsViewModel createSettingsViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        SettingsViewModel settingsViewModel = provider.get(SettingsViewModel.class);
        return Objects.requireNonNull(settingsViewModel);
    }

    @NonNull
    protected final ThemeColor requireThemeColor() {
        return settingsViewModel.loadThemeColorSettingValue();
    };

    // ThemeColorに合わせたインフレーター作成
    @NonNull
    protected final LayoutInflater createThemeColorInflater(LayoutInflater inflater, ThemeColor themeColor) {
        Objects.requireNonNull(inflater);
        Objects.requireNonNull(themeColor);

        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(requireContext(), inflater, themeColor);
        return creator.create();
    }

    /**
     * 戻り値をtrueにすると、ダイアログ枠外、戻るボタンタッチ時にダイアログをキャンセルすることを可能にする。
     * BaseBottomSheetDialogFragment#onCreateView()で呼び出される。
     * */
    protected abstract View createDialogView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected final class PositiveButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Objects.requireNonNull(v);

            handleOnClickPositiveButton(v);
            closeDialog();
        }
    }

    protected final class NegativeButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Objects.requireNonNull(v);

            handleOnClickNegativeButton(v);
            closeDialog();
        }
    }

    private void closeDialog() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }

    /**
     * BaseBottomSheetDialogFragment.PositiveButtonClickListener#onClick()で呼び出される。
     * */
    protected abstract void handleOnClickPositiveButton(@NonNull View v);

    /**
     * BaseBottomSheetDialogFragment.NegativeButtonClickListener#onClick()で呼び出される。
     * */
    protected abstract void handleOnClickNegativeButton(@NonNull View v);

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
     * BaseBottomSheetDialogFragment.onCancel()で呼び出される。
     * */
    protected abstract void handleOnCancel(@NonNull DialogInterface dialog);

    @Override
    public final void dismiss() {
        handleOnDismiss();
        super.dismiss();
    }

    /**
     * BaseBottomSheetDialogFragment.dismiss()で呼び出される。
     * */
    protected abstract void handleOnDismiss();
}
