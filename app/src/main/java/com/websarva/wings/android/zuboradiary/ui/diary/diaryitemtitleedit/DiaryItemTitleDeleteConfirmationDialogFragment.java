package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class DiaryItemTitleDeleteConfirmationDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryItemTitleDeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    public static final String KEY_DELETE_LIST_ITEM_POSITION = "DeleteItemPosition" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_item_title_delete_confirmation_title);
    }

    @Override
    protected String createMessage() {
        String deleteItemTitle =
                DiaryItemTitleDeleteConfirmationDialogFragmentArgs
                        .fromBundle(requireArguments()).getDeleteItemTitle();
        return getString(R.string.dialog_diary_item_title_delete_confirmation_first_message) + deleteItemTitle + getString(
                R.string.dialog_diary_item_title_delete_confirmation_second_message);
    }

    @Override
    protected void handleOnClickPositiveButton(@NonNull DialogInterface dialog, int which) {
        processResults(DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnClickNegativeButton(@NonNull DialogInterface dialog, int which) {
        processResults(DialogInterface.BUTTON_NEGATIVE);
    }

    @Override
    protected boolean isCancelableOtherThanPressingButton() {
        return true;
    }

    @Override
    protected void handleOnCancel(@NonNull DialogInterface dialog) {
        processResults(DialogInterface.BUTTON_NEGATIVE);
    }

    private void processResults(int status) {
        setResult(KEY_SELECTED_BUTTON, status);

        int deleteListItemPosition =
                DiaryItemTitleDeleteConfirmationDialogFragmentArgs
                        .fromBundle(getArguments()).getDeleteListItemPosition();
        setResult(KEY_DELETE_LIST_ITEM_POSITION, deleteListItemPosition);
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }
}
