package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment;

public class ThemeColorPickerDialogFragment extends BaseNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + ThemeColorPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_THEME_COLOR = "SelectedThemeColor" + fromClassName;

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handlePositiveButton(@NonNull View v) {
        int selectedValue = binding.numberPickerFirst.getValue();
        ThemeColor selectedThemeColor = ThemeColor.values()[selectedValue];

        setResult(KEY_SELECTED_THEME_COLOR, selectedThemeColor);
    }

    @Override
    protected void handleNegativeButton(@NonNull View v) {
        // 処理なし
    }

    @Override
    protected void handleCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleDismiss() {

    }

    @Override
    protected void setUpNumberPickers(DialogFragmentNumberPickersBinding binding) {
        int maxNumThemeColors = ThemeColor.values().length;
        binding.numberPickerFirst.setMaxValue(maxNumThemeColors - 1);
        binding.numberPickerFirst.setMinValue(0);
        setUpInitialValue(binding);
        setUpDisplayedValues(binding);
        binding.numberPickerFirst.setWrapSelectorWheel(false);
        binding.numberPickerSecond.setVisibility(View.GONE);
        binding.numberPickerThird.setVisibility(View.GONE);
    }

    private void setUpInitialValue(DialogFragmentNumberPickersBinding binding) {
        ThemeColor currentThemeColor =
                ThemeColorPickerDialogFragmentArgs
                        .fromBundle(requireArguments()).getCurrentThemeColor();
        binding.numberPickerFirst.setValue(currentThemeColor.ordinal()); // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
    }

    private void setUpDisplayedValues(DialogFragmentNumberPickersBinding binding) {
        int maxNumThemeColors = ThemeColor.values().length;
        String[] themeColorList = new String[maxNumThemeColors];
        for (ThemeColor item: ThemeColor.values()) {
            int ordinal = item.ordinal();
            themeColorList[ordinal] = item.toSting(requireContext());
        }
        binding.numberPickerFirst.setDisplayedValues(themeColorList);
    }
}
