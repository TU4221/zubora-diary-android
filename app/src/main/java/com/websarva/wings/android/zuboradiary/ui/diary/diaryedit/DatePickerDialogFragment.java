package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.squareup.moshi.ToJson;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        long currentEpochMilli =
                currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Activity activity = requireActivity();
        MainActivity mainActivity;
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        } else {
            throw new ClassCastException();
        }
        int theme = mainActivity.requireDialogThemeColor().getDatePickerDialogTheme();

        // MEMO:MaterialDatePickerはDialogクラスを作成できないのでダミーDialogを作成して戻り値として返し
        //      MaterialDatePicker#show()でDatePickerDialogを表示する。ダミーDialogも重なって表示されるので、
        //      MaterialDatePickerに追加したリスナーでダミーDialogを閉じる(Dialog#dismiss())。
        Dialog dummyDialog = new Dialog(requireContext());
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                .setTheme(theme)
                .setSelection(currentEpochMilli)
                .build();
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                // 選択日付型変換(EpochMilli -> LocalDate)
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate selectedDate = LocalDate.ofInstant(instant, ZoneId.systemDefault());

                // 選択日付を返す
                NavController navController =
                        NavHostFragment.findNavController(DatePickerDialogFragment.this);
                NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
                if (navBackStackEntry != null) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    savedStateHandle.set(KEY_SELECTED_DATE, selectedDate);
                }

                dummyDialog.dismiss();
            }
        });
        datePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dummyDialog.dismiss();
            }
        });
        datePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dummyDialog.dismiss();
            }
        });
        datePicker.addOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dummyDialog.dismiss();
            }
        });
        datePicker.show(getChildFragmentManager(),"");
        return dummyDialog;
    }
}
