package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

import java.time.LocalDate;

public class DiaryDeleteConfirmationDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryDeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_delete_confirmation_title);
    }

    @Override
    protected String createMessage() {
        LocalDate date =
                DiaryDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteDiaryDate();
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String strDate = dateTimeStringConverter.toStringDate(date);
        return strDate + getString(R.string.dialog_diary_delete_confirmation_message);
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
