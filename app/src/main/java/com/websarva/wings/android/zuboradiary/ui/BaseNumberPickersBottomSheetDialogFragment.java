package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;

import java.util.Objects;

public abstract class BaseNumberPickersBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {
    // View関係
    protected DialogFragmentNumberPickersBinding binding;

    @Override
    @NonNull
    protected final View createDialogView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = createBinding(inflater, container);
        binding.buttonDecision.setOnClickListener(new PositiveButtonClickListener());
        binding.buttonCancel.setOnClickListener(new NegativeButtonClickListener());
        setUpNumberPickers(binding);
        return binding.getRoot();
    }

    @NonNull
    private DialogFragmentNumberPickersBinding createBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {

        // HACK:下記理由から、ThemeColor#getNumberPickerBottomSheetDialogThemeResId()から
        //      ThemeResIdを取得してInflaterを再作成。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
        ThemeColor themeColor = requireThemeColor();
        int themeResId = themeColor.getNumberPickerBottomSheetDialogThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireActivity(), themeResId);
        LayoutInflater _inflater = inflater.cloneInContext(contextWithTheme);

        DialogFragmentNumberPickersBinding binding =
                DialogFragmentNumberPickersBinding.inflate(_inflater, container, false);

        setUpNumberPickerTextColor(binding);

        return binding;
    }

    private void setUpNumberPickerTextColor(DialogFragmentNumberPickersBinding binding) {
        Objects.requireNonNull(binding);

        if (Build.VERSION.SDK_INT >= 29) {
            ThemeColor themeColor = requireThemeColor();
            int onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(getResources());
            binding.numberPickerFirst.setTextColor(onSurfaceVariantColor);
            binding.numberPickerSecond.setTextColor(onSurfaceVariantColor);
            binding.numberPickerThird.setTextColor(onSurfaceVariantColor);
        }
    }

    /**
     * BaseNumberPickersBottomSheetDialogFragment#createDialogView()で呼び出される。
     * */
    protected abstract void setUpNumberPickers(DialogFragmentNumberPickersBinding binding);

    protected final void setResult(String key, Object value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
        savedStateHandle.set(key, value);
    }
}
