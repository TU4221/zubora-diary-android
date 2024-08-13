package com.websarva.wings.android.zuboradiary.ui.diary.editdiary;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.time.LocalDate;
import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment{
    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_DATE = "SelectedDate" + fromClassName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalDate currentDate =
                DatePickerDialogFragmentArgs.fromBundle(requireArguments()).getCurrentDate();
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
