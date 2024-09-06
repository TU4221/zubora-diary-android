package com.websarva.wings.android.zuboradiary.ui.settings;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickerBinding;

public class ThemeColorPickerDialogFragment extends BottomSheetDialogFragment {
    private static final String fromClassName = "From" + ThemeColorPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_THEME_COLOR = "SelectedThemeColor" + fromClassName;

    // View関係
    private DialogFragmentNumberPickerBinding binding;

    // Navigation関係
    private NavController navController;

    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
            @Nullable android.os.Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

        // データバインディング設定
        this.binding =
                DialogFragmentNumberPickerBinding.inflate(inflater, container, false);

        // View設定
        ThemeColor currentThemeColor =
                ThemeColorPickerDialogFragmentArgs
                        .fromBundle(requireArguments()).getCurrentThemeColor();
        int maxNumThemeColors = ThemeColor.values().length;
        this.binding.numberPicker.setMaxValue(maxNumThemeColors - 1);
        this.binding.numberPicker.setMinValue(0);
        this.binding.numberPicker.setValue(currentThemeColor.ordinal()); // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
        this.binding.numberPicker.setWrapSelectorWheel(false);

        String[] themeColorList = new String[maxNumThemeColors];
        for (ThemeColor item: ThemeColor.values()) {
            int ordinal = item.ordinal();
            themeColorList[ordinal] = item.toSting(requireContext());
        }
        this.binding.numberPicker.setDisplayedValues(themeColorList);

        this.binding.buttonDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavBackStackEntry navBackStackEntry =
                        navController.getPreviousBackStackEntry();
                if (navBackStackEntry == null) {
                    throw new NullPointerException();
                }
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();

                int selectedValue =
                        ThemeColorPickerDialogFragment.this.binding.numberPicker.getValue();
                ThemeColor selectedThemeColor = ThemeColor.values()[selectedValue];

                savedStateHandle.set(KEY_SELECTED_THEME_COLOR, selectedThemeColor);
                navController.navigateUp();
            }
        });

        this.binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemeColorPickerDialogFragment.this.navController.navigateUp();
            }
        });

        return this.binding.getRoot();
    }
}
