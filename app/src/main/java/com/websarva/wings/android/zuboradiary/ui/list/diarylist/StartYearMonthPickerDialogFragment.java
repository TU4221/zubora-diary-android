package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentTwoNumberPickersBinding;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public class StartYearMonthPickerDialogFragment extends BottomSheetDialogFragment {
    private static final String fromClassName = "From" + StartYearMonthPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR_MONTH = "SelectedYearMonth" + fromClassName;

    // View関係
    private DialogFragmentTwoNumberPickersBinding binding;

    // Navigation関係
    private NavController navController;

    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
            @Nullable android.os.Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Navigation設定
        navController = NavHostFragment.findNavController(this);

        // データバインディング設定
        binding =
                DialogFragmentTwoNumberPickersBinding.inflate(inflater, container, false);

        // View設定
        LocalDate today = LocalDate.now();
        Year maxYear =
                StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).getYearMaxValue();
        Year minYear =
                StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).getYearMinValue();
        binding.numberPickerFirst.setMaxValue(maxYear.getValue());
        binding.numberPickerFirst.setMinValue(minYear.getValue());
        binding.numberPickerFirst.setValue(today.getYear());
        binding.numberPickerFirst.setWrapSelectorWheel(false);
        binding.numberPickerSecond.setMaxValue(12);
        binding.numberPickerSecond.setMinValue(1);
        binding.numberPickerSecond.setValue(today.getMonthValue());
        binding.numberPickerSecond.setWrapSelectorWheel(false);

        binding.buttonDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                if (navBackStackEntry == null) {
                    // TODO:assert
                    return;
                }
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                int selectedYear =
                        binding.numberPickerFirst.getValue();
                int selectedMonth =
                        binding.numberPickerSecond.getValue();
                YearMonth selectedYearMonth = YearMonth.of(selectedYear, selectedMonth);
                savedStateHandle.set(KEY_SELECTED_YEAR_MONTH, selectedYearMonth);
                navController.navigateUp();
            }
        });

        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

        return binding.getRoot();
    }
}
