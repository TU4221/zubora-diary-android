package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;

public class LoadExistingDiaryDialogFragment extends BaseAlertDialogFragment {
    private static final String fromClassName =
            "From" + LoadExistingDiaryDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_load_Existing_diary_title);
    }

    @Override
    protected String createMessage() {
        LocalDate loadDiaryDate =
                LoadExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getLoadDiaryDate();
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String stringLoadDiaryDate = dateTimeStringConverter.toStringDate(loadDiaryDate);
        return stringLoadDiaryDate + getString(R.string.dialog_load_Existing_diary_message);
    }

    @Override
    protected void handlePositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleNegativeButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleCancel(@NonNull DialogInterface dialog) {
        // 処理なし
    }

    @Override
    protected void handleDismiss() {
        // 処理なし
    }
}
