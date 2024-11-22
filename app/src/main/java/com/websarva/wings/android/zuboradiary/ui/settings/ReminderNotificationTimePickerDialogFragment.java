package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;
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
    protected void handleOnPositiveButtonClick(@NonNull View v) {
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
    protected void handleOnNegativeButtonClick(@NonNull View v) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }

    @Override
    protected void setUpNumberPickers(DialogFragmentNumberPickersBinding binding) {
        LocalTime localTime = LocalTime.now();
        this.binding.numberPickerFirst.setMaxValue(23);
        this.binding.numberPickerFirst.setMinValue(0);
        this.binding.numberPickerFirst.setFormatter(new valueFormatter());
        this.binding.numberPickerFirst.setWrapSelectorWheel(false);
        this.binding.numberPickerFirst.setValue(localTime.getHour());
        this.binding.numberPickerSecond.setMaxValue(59);
        this.binding.numberPickerSecond.setMinValue(0);
        this.binding.numberPickerSecond.setFormatter(new valueFormatter());
        this.binding.numberPickerSecond.setWrapSelectorWheel(false);
        this.binding.numberPickerSecond.setValue(localTime.getMinute());
        this.binding.numberPickerThird.setVisibility(View.GONE);
    }

    private static class valueFormatter implements NumberPicker.Formatter {
        @Override
        public String format(int value) {
            return String.format(Locale.getDefault(),"%02d", value);
        }
    }
}
