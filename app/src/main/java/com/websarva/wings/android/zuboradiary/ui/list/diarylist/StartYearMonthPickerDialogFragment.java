package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentTwoNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseTwoNumberPickersBottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.Year;

public class StartYearMonthPickerDialogFragment extends BaseTwoNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + StartYearMonthPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR_MONTH = "SelectedYearMonth" + fromClassName;

    @Override
    protected void setUpNumberPickers(DialogFragmentTwoNumberPickersBinding binding) {
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
    }

    @Override
    protected void handlePositiveButton(@NonNull View v) {
        setResultSelectedYearMonth(KEY_SELECTED_YEAR_MONTH);
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
