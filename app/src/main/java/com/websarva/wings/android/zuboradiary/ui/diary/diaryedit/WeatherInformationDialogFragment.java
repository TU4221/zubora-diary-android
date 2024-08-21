package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateConverter;

import java.time.LocalDate;

public class WeatherInformationDialogFragment extends DialogFragment {
    private static final String fromClassName =
            "From" + WeatherInformationDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("確認");

        LocalDate loadDiaryDate =
                WeatherInformationDialogFragmentArgs.fromBundle(requireArguments()).getTargetDiaryDate();
        DateConverter dateConverter = new DateConverter();
        String stringDate = dateConverter.toStringLocalDate(loadDiaryDate);
        String message = stringDate + "の天気情報を取得しますか。";

        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_load_Existing_diary_yes, new custumOnClickListener());
        builder.setNegativeButton(R.string.dialog_load_Existing_diary_no, new custumOnClickListener());
        return builder.create();
    }

    private class custumOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            NavController navController =
                    NavHostFragment
                            .findNavController(WeatherInformationDialogFragment.this);
            NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
            if (navBackStackEntry != null) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                savedStateHandle.set(KEY_SELECTED_BUTTON, which);
            }
        }
    }
}
