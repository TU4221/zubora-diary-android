package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment {
    private static final String fromClassName = "From" + DatePickerDialogFragment.class.getName();
    public static final String KEY_SELECTED_YEAR = "SelectedYear" + fromClassName;
    public static final String KEY_SELECTED_MONTH = "SelectedMonth" + fromClassName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("20240527", DatePickerDialogFragment.class.getName());
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        // android.R.style.Theme_Holo_Dialog でドラムロールに変更。
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireActivity(),
                android.R.style.Theme_Holo_Dialog,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        NavController navController =
                                NavHostFragment
                                        .findNavController(DatePickerDialogFragment.this);
                        SavedStateHandle savedStateHandle =
                                navController.getPreviousBackStackEntry().getSavedStateHandle();
                        savedStateHandle.set(KEY_SELECTED_YEAR, year);
                        savedStateHandle.set(KEY_SELECTED_MONTH, month + 1);
                    }
                },
                year,
                month,
                day
        );
        // 日付選択ダイアログの日付選択インスタンスを取得。
        DatePicker datePicker = datePickerDialog.getDatePicker();

        // 日付選択インスタンスから年月日の日選択を削除。
        int datePickerId
                = Resources.getSystem().getIdentifier("day", "id", "android");
        datePicker.findViewById(datePickerId).setVisibility(View.GONE);
        return datePickerDialog;
    }
}
