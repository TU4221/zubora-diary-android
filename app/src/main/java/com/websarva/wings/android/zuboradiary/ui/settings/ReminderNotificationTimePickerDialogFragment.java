package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment;

import java.time.LocalTime;
import java.util.Locale;

// TODO:MaterialTimePickerに置き換える？
public class ReminderNotificationTimePickerDialogFragment extends BaseNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + ReminderNotificationTimePickerDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    static final String KEY_SELECTED_TIME = "SelectedTIME" + fromClassName;

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handlePositiveButton(@NonNull View v) {
        setResultSelectedYearMonth();
    }

    private void setResultSelectedYearMonth() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);

        int selectedHourValue = binding.numberPickerFirst.getValue();
        int selectedMinuteValue = binding.numberPickerSecond.getValue();
        LocalTime selectedTime = LocalTime.of(selectedHourValue, selectedMinuteValue);
        setResult(KEY_SELECTED_TIME, selectedTime);
    }

    @Override
    protected void handleNegativeButton(@NonNull View v) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected void handleCancel(@NonNull DialogInterface dialog) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected void handleDismiss() {
        // 処理なし
    }

    @Override
    protected void setUpNumberPickers() {
        LocalTime localTime = LocalTime.now();
        binding.numberPickerFirst.setMaxValue(23);
        binding.numberPickerFirst.setMinValue(0);
        binding.numberPickerFirst.setFormatter(new valueFormatter());
        binding.numberPickerFirst.setWrapSelectorWheel(false);
        binding.numberPickerFirst.setValue(localTime.getHour());
        binding.numberPickerSecond.setMaxValue(59);
        binding.numberPickerSecond.setMinValue(0);
        binding.numberPickerSecond.setFormatter(new valueFormatter());
        binding.numberPickerSecond.setWrapSelectorWheel(false);
        binding.numberPickerSecond.setValue(localTime.getMinute());
        binding.numberPickerThird.setVisibility(View.GONE);
    }

    private static class valueFormatter implements NumberPicker.Formatter {
        @Override
        public String format(int value) {
            return String.format(Locale.getDefault(),"%02d", value);
        }
    }
}
