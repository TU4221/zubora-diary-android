package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment;

import java.time.DayOfWeek;
import java.util.Objects;

public class CalendarStartDayPickerDialogFragment extends BaseNumberPickersBottomSheetDialogFragment {

    private static final String FROM_CLASS_NAME = "From" + CalendarStartDayPickerDialogFragment.class.getName();
    static final String KEY_SELECTED_DAY_OF_WEEK = "SelectedDayOfWeek" + FROM_CLASS_NAME;

    @Override
    public boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnClickPositiveButton(@NonNull View v) {
        setResultSelectedDayOfWeek();
    }

    private void setResultSelectedDayOfWeek() {
        int selectedValue = binding.numberPickerFirst.getValue();
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        DayOfWeek selectedDayOfWeek;
        if (selectedValue == 0) {
            selectedDayOfWeek = DayOfWeek.SUNDAY;
        } else {
            selectedDayOfWeek = DayOfWeek.of(selectedValue);
        }
        setResult(KEY_SELECTED_DAY_OF_WEEK, selectedDayOfWeek);
    }

    @Override
    protected void handleOnClickNegativeButton(@NonNull View v) {
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
        int maxNumDaysOfWeek = DayOfWeek.values().length;
        this.binding.numberPickerFirst.setMaxValue(maxNumDaysOfWeek - 1);
        this.binding.numberPickerFirst.setMinValue(0);
        setUpInitialValue(this.binding);
        setUpDisplayedValues(this.binding);
        this.binding.numberPickerFirst.setWrapSelectorWheel(false);
        this.binding.numberPickerSecond.setVisibility(View.GONE);
        this.binding.numberPickerThird.setVisibility(View.GONE);
    }

    private void setUpInitialValue(DialogFragmentNumberPickersBinding binding) {
        DayOfWeek currentCalendarStartDayOfWeek =
                CalendarStartDayPickerDialogFragmentArgs
                        .fromBundle(requireArguments()).getInitialValue();
        int initialValue;
        // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
        if (currentCalendarStartDayOfWeek == DayOfWeek.SUNDAY) {
            initialValue = 0;
        } else {
            initialValue = currentCalendarStartDayOfWeek.getValue();
        }
        binding.numberPickerFirst.setValue(initialValue); // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
    }

    private void setUpDisplayedValues(DialogFragmentNumberPickersBinding binding) {
        int maxNumDaysOfWeek = DayOfWeek.values().length;
        String[] dayOfWeekList = new String[maxNumDaysOfWeek];
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            int dayOfWeekNumber;
            // MEMO:DayOfWeekはMonday～Sundayの値が1～7となる。Sundayを先頭に表示させたいため、下記コード記述。
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                dayOfWeekNumber = 0;
            } else {
                dayOfWeekNumber = dayOfWeek.getValue();
            }
            DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(requireContext());
            dayOfWeekList[dayOfWeekNumber] = dayOfWeekStringConverter.toCalendarStartDayOfWeek(dayOfWeek);
        }
        binding.numberPickerFirst.setDisplayedValues(dayOfWeekList);
    }
}
