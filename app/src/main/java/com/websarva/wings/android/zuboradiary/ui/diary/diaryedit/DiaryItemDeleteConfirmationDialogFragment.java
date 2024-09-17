package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class DiaryItemDeleteConfirmationDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryItemDeleteConfirmationDialogFragment.class.getName();
    public static final String KEY_DELETE_ITEM_NUMBER = "DeleteItemNumber" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_item_delete_confirmation_title);
    }

    @Override
    protected String createMessage() {
        int deleteItemNumber =
                DiaryItemDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteItemNumber();
        return getString(R.string.dialog_diary_item_delete_confirmation_first_message) + deleteItemNumber + getString(R.string.dialog_diary_item_delete_confirmation_second_message);
    }

    @Override
    protected void handlePositiveButton() {
        int deleteItemNumber =
                DiaryItemDeleteConfirmationDialogFragmentArgs.fromBundle(requireArguments()).getDeleteItemNumber();
        setResult(KEY_DELETE_ITEM_NUMBER, deleteItemNumber);
    }

    @Override
    protected void handleNegativeButton() {
        // 処理なし
    }

    @Override
    protected void handleCancel() {
        // 処理なし
    }

    @Override
    protected void handleDismiss() {
        // 処理なし
    }
}
