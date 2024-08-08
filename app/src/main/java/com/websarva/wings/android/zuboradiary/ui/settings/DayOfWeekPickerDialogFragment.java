package com.websarva.wings.android.zuboradiary.ui.settings;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekNameResIdGetter;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickerBinding;

import java.time.DayOfWeek;

public class DayOfWeekPickerDialogFragment extends BottomSheetDialogFragment {
    private static final String FROM_CLASS_NAME = "From" + DayOfWeekPickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_DAY_OF_WEEK = "SelectedDayOfWeek" + FROM_CLASS_NAME;

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
        DayOfWeek currentDayOfWeek =
                DayOfWeekPickerDialogFragmentArgs.fromBundle(requireArguments()).getCurrentCalendarStartDayOfWeek();
        int maxNumDaysOfWeek = DayOfWeek.values().length;
        this.binding.numberPicker.setMaxValue(maxNumDaysOfWeek - 1);
        this.binding.numberPicker.setMinValue(0);
        int initialValue;
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        if (currentDayOfWeek == DayOfWeek.SUNDAY) {
            initialValue = 0;
        } else {
            initialValue = currentDayOfWeek.getValue();
        }
        this.binding.numberPicker.setValue(initialValue); // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
        this.binding.numberPicker.setWrapSelectorWheel(false);

        String[] dayOfWeekList = new String[maxNumDaysOfWeek];
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            int dayOfWeekNumber;
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                dayOfWeekNumber = 0;
            } else {
                dayOfWeekNumber = dayOfWeek.getValue();
            }
            DayOfWeekNameResIdGetter dayOfWeekNameResIdGetter = new DayOfWeekNameResIdGetter();
            Integer resId = dayOfWeekNameResIdGetter.getResId(dayOfWeek);
            dayOfWeekList[dayOfWeekNumber] = getString(resId);
        }
        this.binding.numberPicker.setDisplayedValues(dayOfWeekList);

        this.binding.buttonDecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedValue =
                        DayOfWeekPickerDialogFragment.this.binding.numberPicker.getValue();
                // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
                DayOfWeek selectedDayOfWeek;
                if (selectedValue == 0) {
                    selectedDayOfWeek = DayOfWeek.SUNDAY;
                } else {
                    selectedDayOfWeek = DayOfWeek.of(selectedValue);
                }
                SavedStateHandle savedStateHandle =
                        DayOfWeekPickerDialogFragment.this.navController
                                .getPreviousBackStackEntry().getSavedStateHandle();
                savedStateHandle.set(KEY_SELECTED_DAY_OF_WEEK, selectedDayOfWeek);
                DayOfWeekPickerDialogFragment.this.navController.navigateUp();
            }
        });

        this.binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayOfWeekPickerDialogFragment.this.navController.navigateUp();
            }
        });

        return this.binding.getRoot();
    }
}
