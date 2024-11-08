package com.websarva.wings.android.zuboradiary.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Objects;

public enum AppError {

    DIARY_LOADING(R.string.dialog_message_title_access_error, R.string.dialog_message_message_diary_loading_error),
    DIARY_SAVING(R.string.dialog_message_title_access_error, R.string.dialog_message_message_diary_saving_error),
    DIARY_DELETE(R.string.dialog_message_title_access_error, R.string.dialog_message_message_diary_delete_error),
    DIARY_INFORMATION_LOADING(R.string.dialog_message_title_access_error, R.string.dialog_message_message_diary_information_loading_error),
    DIARY_ITEM_TITLE_HISTORY_LOADING(R.string.dialog_message_title_access_error, R.string.dialog_message_message_item_title_selection_history_loading_error),
    DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE(R.string.dialog_message_title_access_error, R.string.dialog_message_message_item_title_selection_history_item_delete_error),
    SETTING_LOADING(R.string.dialog_message_title_access_error, R.string.dialog_message_message_setting_loading_error),
    SETTING_UPDATE(R.string.dialog_message_title_access_error, R.string.dialog_message_message_setting_update_error),
    WEATHER_INFORMATION_LOADING(R.string.dialog_message_title_connection_error, R.string.dialog_message_message_weather_information_loading_error);

    private final int dialogTitleStringResId;
    private final int dialogMessageStringResId;

    AppError(int dialogTitleStringResId, int dialogMessageStringResId) {
        this.dialogTitleStringResId = dialogTitleStringResId;
        this.dialogMessageStringResId = dialogMessageStringResId;
    }

    @NonNull
    public String getDialogTitle(Context context) {
        Objects.requireNonNull(context);

        String string = context.getString(dialogTitleStringResId);
        return Objects.requireNonNull(string);
    }

    @NonNull
    public String getDialogMessage(Context context) {
        Objects.requireNonNull(context);

        String string = context.getString(dialogMessageStringResId);
        return Objects.requireNonNull(string);
    }
}
