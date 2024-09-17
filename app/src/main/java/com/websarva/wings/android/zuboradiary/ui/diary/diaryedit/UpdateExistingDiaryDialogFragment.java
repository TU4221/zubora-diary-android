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

public class UpdateExistingDiaryDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + UpdateExistingDiaryDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    public static final String KEY_UPDATE_TYPE = "UpdateType" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_update_Existing_diary_title);
    }

    @Override
    protected String createMessage() {
        LocalDate updateDiaryDate =
                UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getUpdateDiaryDate();
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String stringUpdateDiaryDate = dateTimeStringConverter.toStringDate(updateDiaryDate);
        return stringUpdateDiaryDate + getString(R.string.dialog_update_Existing_diary_message);
    }

    @Override
    protected void handlePositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);

        int updateType =
                UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getUpdateType();
        setResult(KEY_UPDATE_TYPE, updateType);
    }

    @Override
    protected void handleNegativeButton(@NonNull DialogInterface dialog, int which) {
        // 処理なし
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
