package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class LoadExistingDiaryDialogFragment extends BaseAlertDialogFragment {
    private static final String fromClassName =
            "From" + LoadExistingDiaryDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_load_Existing_diary_title);
    }

    @Override
    protected String createMessage() {
        LocalDate loadingDiaryDate =
                LoadExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getLoadDiaryDate();
        Objects.requireNonNull(loadingDiaryDate);

        DateTimeStringConverter converter = new DateTimeStringConverter();
        String loadingDiaryDateString = converter.toYearMonthDayWeek(loadingDiaryDate);
        return loadingDiaryDateString + getString(R.string.dialog_load_Existing_diary_message);
    }

    @Override
    protected void handleOnClickPositiveButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnClickNegativeButton(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }
}
