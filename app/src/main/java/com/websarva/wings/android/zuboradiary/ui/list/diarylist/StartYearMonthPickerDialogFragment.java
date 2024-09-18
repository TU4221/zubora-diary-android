package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public class StartYearMonthPickerDialogFragment extends BaseNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + StartYearMonthPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR_MONTH = "SelectedYearMonth" + fromClassName;

    @Override
    protected void setUpNumberPickers(DialogFragmentNumberPickersBinding binding) {
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
        binding.numberPickerThird.setVisibility(View.GONE);
    }

    @Override
    protected void handlePositiveButton(@NonNull View v) {
        setResultSelectedYearMonth();
    }

    private void setResultSelectedYearMonth() {
        int selectedYear =
                binding.numberPickerFirst.getValue();
        int selectedMonth =
                binding.numberPickerSecond.getValue();
        YearMonth selectedYearMonth = YearMonth.of(selectedYear, selectedMonth);

        setResult(KEY_SELECTED_YEAR_MONTH, selectedYearMonth);
    }

    @Override
    protected void handleNegativeButton(@NonNull View v) {
        // 処理なし
    }

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleDismiss() {
        // 処理なし
    }
}
