package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;


import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.ui.BaseDatePickerDialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class DatePickerDialogFragment extends BaseDatePickerDialogFragment {

    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    static final String KEY_SELECTED_DATE = "SelectedDate" + fromClassName;

    @Override
    protected LocalDate createInitialDate() {
        return DatePickerDialogFragmentArgs.fromBundle(requireArguments()).getDate();
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull LocalDate selectedDate) {
        // 選択日付を返す
        NavController navController =
                NavHostFragment.findNavController(DatePickerDialogFragment.this);
        Objects.requireNonNull(navController);
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
        savedStateHandle.set(KEY_SELECTED_DATE, selectedDate);
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
    protected void handleOnDismiss(@NonNull DialogInterface dialog) {
        // 処理なし
    }
}
