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
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class WeatherInformationDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + WeatherInformationDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_weather_information_title);
    }

    @Override
    protected String createMessage() {
        LocalDate loadingDiaryDate =
                WeatherInformationDialogFragmentArgs.fromBundle(requireArguments()).getTargetDiaryDate();
        Objects.requireNonNull(loadingDiaryDate);

        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String dateString = dateTimeStringConverter.toStringDate(loadingDiaryDate);
        return dateString + getString(R.string.dialog_weather_information_message);
    }

    @Override
    protected void handleOnClickPositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnClickNegativeButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }
}
