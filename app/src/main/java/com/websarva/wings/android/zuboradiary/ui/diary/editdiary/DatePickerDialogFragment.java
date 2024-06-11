package com.websarva.wings.android.zuboradiary.ui.diary.editdiary;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment{
    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR = "SelectedYear" + fromClassName;
    public static final String KEY_SELECTED_MONTH = "SelectedMonth" + fromClassName;
    public static final String KEY_SELECTED_DAY_OF_MONTH = "SelectedDayOfMonth" + fromClassName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(
                requireActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        NavController navController =
                                NavHostFragment.findNavController(
                                        DatePickerDialogFragment.this);
                        SavedStateHandle savedStateHandle =
                                navController.getPreviousBackStackEntry().getSavedStateHandle();
                        savedStateHandle.set(KEY_SELECTED_YEAR, year);
                        savedStateHandle.set(KEY_SELECTED_MONTH, month + 1);
                        savedStateHandle.set(KEY_SELECTED_DAY_OF_MONTH, dayOfMonth);
                    }
                },
                year,
                month,
                day
        );
    }
}
