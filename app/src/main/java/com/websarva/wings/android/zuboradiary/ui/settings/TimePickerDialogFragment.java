package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentThreeNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseThreeNumberPickersBottomSheetDialogFragment;

import java.time.LocalTime;

// TODO:MaterialTimePickerに置き換える？
public class TimePickerDialogFragment extends BaseThreeNumberPickersBottomSheetDialogFragment {

    private static final String fromClassName = "From" + TimePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_HOUR = "SelectedHour" + fromClassName;
    public static final String KEY_SELECTED_MINUTE = "SelectedMinute" + fromClassName;

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handlePositiveButton(@NonNull View v) {
        // TODO:LocalTimeクラスを代入する方が良いかも・・・
        int selectedHourValue = binding.numberPickerFirst.getValue();
        setResult(KEY_SELECTED_HOUR, selectedHourValue);

        int selectedMinuteValue = binding.numberPickerSecond.getValue();
        setResult(KEY_SELECTED_MINUTE,selectedMinuteValue);
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
        // 処理なし
    }

    private void setResults(Integer hour, Integer minute) {
        setResult(KEY_SELECTED_HOUR, hour);
        setResult(KEY_SELECTED_MINUTE, minute);
    }

    @Override
    protected void setUpNumberPickers(DialogFragmentThreeNumberPickersBinding binding) {
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
            return String.format("%02d", value);
        }
    }
}
