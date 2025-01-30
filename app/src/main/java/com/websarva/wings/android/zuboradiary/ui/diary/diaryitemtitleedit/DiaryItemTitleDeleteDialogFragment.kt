package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class DiaryItemTitleDeleteDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryItemTitleDeleteDialogFragment.class.getName();
    public static final String KEY_SELECTED_BUTTON = "SelectedButton" + fromClassName;
    public static final String KEY_DELETE_LIST_ITEM_POSITION = "DeleteItemPosition" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_item_title_delete_title);
    }

    @Override
    protected String createMessage() {
        String deleteItemTitle =
                DiaryItemTitleDeleteDialogFragmentArgs
                        .fromBundle(requireArguments()).getItemTitle();
        return getString(R.string.dialog_diary_item_title_delete_first_message) + deleteItemTitle + getString(
                R.string.dialog_diary_item_title_delete_second_message);
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which) {
        processResults(DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    protected void handleOnNegativeButtonClick(@NonNull DialogInterface dialog, int which) {
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
                DiaryItemTitleDeleteDialogFragmentArgs
                        .fromBundle(getArguments()).getItemPosition();
        setResult(KEY_DELETE_LIST_ITEM_POSITION, deleteListItemPosition);
    }

    @Override
    protected void handleOnDismiss() {
        // 処理なし
    }
}
