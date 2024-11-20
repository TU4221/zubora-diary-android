package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class UpdateExistingDiaryDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + UpdateExistingDiaryDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_update_Existing_diary_title);
    }

    @Override
    protected String createMessage() {
        LocalDate updateDiaryDate =
                UpdateExistingDiaryDialogFragmentArgs.fromBundle(requireArguments()).getDate();
        Objects.requireNonNull(updateDiaryDate);

        DateTimeStringConverter converter = new DateTimeStringConverter();
        String updateDiaryDateString = converter.toYearMonthDayWeek(updateDiaryDate);
        return updateDiaryDateString + getString(R.string.dialog_update_Existing_diary_message);
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
