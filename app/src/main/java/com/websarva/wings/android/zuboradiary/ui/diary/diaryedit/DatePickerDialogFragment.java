package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.squareup.moshi.ToJson;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import dagger.internal.Preconditions;

public class DatePickerDialogFragment extends DialogFragment{
    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_DATE = "SelectedDate" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalDate currentDate =
                DatePickerDialogFragmentArgs.fromBundle(requireArguments()).getCurrentDate();
        // TODO:ThemeColor保留
        return new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        NavController navController =
                                NavHostFragment.findNavController(
                                        DatePickerDialogFragment.this);
                        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                        if (navBackStackEntry != null) {
                            SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                            savedStateHandle.set(KEY_SELECTED_DATE, selectedDate);
                        }
                    }
                },
                currentDate.getYear(),
                currentDate.getMonthValue() - 1,
                currentDate.getDayOfMonth()
        );
    }
}
