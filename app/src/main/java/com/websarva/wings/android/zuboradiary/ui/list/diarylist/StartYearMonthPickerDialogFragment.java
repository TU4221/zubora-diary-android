package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Objects;

public class StartYearMonthPickerDialogFragment extends BaseNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + StartYearMonthPickerDialogFragment.class.getName();
    static final String KEY_SELECTED_YEAR_MONTH = "SelectedYearMonth" + fromClassName;

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull View v) {
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
    protected void handleOnNegativeButtonClick(@NonNull View v) {
        // 処理なし
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }

    @Override
    protected void setUpNumberPickers(DialogFragmentNumberPickersBinding binding) {
        LocalDate today = LocalDate.now();
        Year maxYear =
                StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).getMaxYear();
        Year minYear =
                StartYearMonthPickerDialogFragmentArgs.fromBundle(requireArguments()).getMinYear();
        this.binding.numberPickerFirst.setMaxValue(maxYear.getValue());
        this.binding.numberPickerFirst.setMinValue(minYear.getValue());
        this.binding.numberPickerFirst.setValue(today.getYear());
        this.binding.numberPickerFirst.setWrapSelectorWheel(false);
        this.binding.numberPickerSecond.setMaxValue(12);
        this.binding.numberPickerSecond.setMinValue(1);
        this.binding.numberPickerSecond.setValue(today.getMonthValue());
        this.binding.numberPickerSecond.setWrapSelectorWheel(false);
        this.binding.numberPickerThird.setVisibility(View.GONE);
    }
}
