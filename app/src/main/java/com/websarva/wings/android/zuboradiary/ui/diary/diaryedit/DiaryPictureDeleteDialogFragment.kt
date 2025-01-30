package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class DiaryPictureDeleteDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryPictureDeleteDialogFragment.class.getName();
    static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_attached_picture_delete_title);
    }

    @Override
    protected String createMessage() {
        return getString(R.string.dialog_diary_attached_picture_delete_message);
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
