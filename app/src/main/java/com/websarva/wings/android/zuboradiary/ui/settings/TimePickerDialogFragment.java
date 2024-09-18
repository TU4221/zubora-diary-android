package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentThreeNumberPickersBinding;

import java.time.LocalTime;

public class TimePickerDialogFragment extends BottomSheetDialogFragment {
    private static final String fromClassName = "From" + TimePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_HOUR = "SelectedHour" + fromClassName;
    public static final String KEY_SELECTED_MINUTE = "SelectedMinute" + fromClassName;

    // View関係
    private DialogFragmentThreeNumberPickersBinding binding;

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
                DialogFragmentThreeNumberPickersBinding.inflate(inflater, container, false);

        // View設定
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

        this.binding.buttonDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedHourValue = binding.numberPickerFirst.getValue();
                int selectedMinuteValue = binding.numberPickerSecond.getValue();
                SavedStateHandle savedStateHandle =
                        navController.getPreviousBackStackEntry().getSavedStateHandle();
                closeDialog(selectedHourValue, selectedMinuteValue);
            }
        });

        this.binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog(null, null);
            }
        });

        setCancelable(true);

        return this.binding.getRoot();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        closeDialog(null, null);
    }

    private class valueFormatter implements NumberPicker.Formatter {
        @Override
        public String format(int value) {
            return String.format("%02d", value);
        }
    }

    private void closeDialog(Integer hour, Integer minute) {
        SavedStateHandle savedStateHandle =
                TimePickerDialogFragment.this.navController
                        .getPreviousBackStackEntry().getSavedStateHandle();
        savedStateHandle.set(KEY_SELECTED_HOUR, hour);
        savedStateHandle.set(KEY_SELECTED_MINUTE, minute);
        TimePickerDialogFragment.this.navController.navigateUp();
    }
}
