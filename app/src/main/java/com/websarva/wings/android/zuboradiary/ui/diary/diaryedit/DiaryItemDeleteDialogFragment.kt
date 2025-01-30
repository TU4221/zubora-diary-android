package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.ui.BaseAlertDialogFragment;

public class DiaryItemDeleteDialogFragment extends BaseAlertDialogFragment {

    private static final String fromClassName =
            "From" + DiaryItemDeleteDialogFragment.class.getName();
    static final String KEY_DELETE_ITEM_NUMBER = "DeleteItemNumber" + fromClassName;

    @Override
    protected String createTitle() {
        return getString(R.string.dialog_diary_item_delete_title);
    }

    @Override
    protected String createMessage() {
        ItemNumber deleteItemNumber =
                DiaryItemDeleteDialogFragmentArgs
                        .fromBundle(requireArguments()).getItemNumber();
        return getString(R.string.dialog_diary_item_delete_first_message) + deleteItemNumber + getString(R.string.dialog_diary_item_delete_second_message);
    }

    @Override
    protected void handleOnPositiveButtonClick(@NonNull DialogInterface dialog, int which) {
        ItemNumber deleteItemNumber =
                DiaryItemDeleteDialogFragmentArgs
                        .fromBundle(requireArguments()).getItemNumber();
        setResult(KEY_DELETE_ITEM_NUMBER, deleteItemNumber);
    }

    @Override
    protected void handleOnNegativeButtonClick(@NonNull DialogInterface dialog, int which) {
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
