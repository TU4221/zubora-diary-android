package com.websarva.wings.android.zuboradiary.ui.list;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentDatePickerBinding;

import java.time.LocalDate;

public class DatePickerDialogFragment extends BottomSheetDialogFragment {
    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR = "SelectedYear" + fromClassName;
    public static final String KEY_SELECTED_MONTH = "SelectedMonth" + fromClassName;

    // View関係
    private DialogFragmentDatePickerBinding binding;

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
                DialogFragmentDatePickerBinding.inflate(inflater, container, false);

        // View設定
        LocalDate today = LocalDate.now();
        int yearMaxValue =
                DatePickerDialogFragmentArgs.fromBundle(requireArguments()).getYearMaxValue();
        int yearMinValue =
                DatePickerDialogFragmentArgs.fromBundle(requireArguments()).getYearMinValue();
        this.binding.numberPickerYear.setMaxValue(yearMaxValue);
        this.binding.numberPickerYear.setMinValue(yearMinValue);
        this.binding.numberPickerYear.setValue(today.getYear());
        this.binding.numberPickerYear.setWrapSelectorWheel(false);
        this.binding.numberPickerMonth.setMaxValue(12);
        this.binding.numberPickerMonth.setMinValue(1);
        this.binding.numberPickerMonth.setValue(today.getMonthValue());
        this.binding.numberPickerMonth.setWrapSelectorWheel(false);

        this.binding.buttonDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedYear =
                        DatePickerDialogFragment.this.binding.numberPickerYear.getValue();
                int selectedMonth =
                        DatePickerDialogFragment.this.binding.numberPickerMonth.getValue();
                SavedStateHandle savedStateHandle =
                        DatePickerDialogFragment.this.navController
                                .getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle.set(KEY_SELECTED_YEAR, selectedYear);
                savedStateHandle.set(KEY_SELECTED_MONTH, selectedMonth);
                DatePickerDialogFragment.this.navController.navigateUp();
            }
        });

        this.binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialogFragment.this.navController.navigateUp();
            }
        });

        return this.binding.getRoot();
    }
}
