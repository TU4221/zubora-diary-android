package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        // android.R.style.Theme_Holo_Dialog でドラムロールに変更。
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                android.R.style.Theme_Holo_Dialog,
                this, year, month, day
        );
        // 日付選択ダイアログの日付選択インスタンスを取得。
        DatePicker datePicker = datePickerDialog.getDatePicker();

        // 日付選択インスタンスから年月日の日選択を削除。
        int datePickerId
                = Resources.getSystem().getIdentifier("day", "id", "android");
        datePicker.findViewById(datePickerId).setVisibility(View.GONE);
        return datePickerDialog;
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        Bundle result = new Bundle();
        result.putInt("Year", year);
        result.putInt("Month", month + 1);
        result.putInt("DayOfMonth", day);
        getParentFragmentManager().setFragmentResult(
                "ToListFragment_DatePickerDialogFragmentRequestKey",
                result
        );
    }
}
