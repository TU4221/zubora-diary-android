package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class DiaryLoadingDialogFragment extends BaseAlertDialogFragment {
    private static final String fromClassName =
            "From" + DiaryLoadingDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_loading_title);
    }

    @Override
    protected String createMessage() {
        LocalDate diaryDate =
                DiaryLoadingDialogFragmentArgs.fromBundle(requireArguments()).getDate();
        Objects.requireNonNull(diaryDate);

        DateTimeStringConverter converter = new DateTimeStringConverter();
        String diaryDateString = converter.toYearMonthDayWeek(diaryDate);
        return diaryDateString + getString(R.string.dialog_diary_loading_message);
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which) {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnNegativeButtonClick(@NonNull DialogInterface dialog, int which) {
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
