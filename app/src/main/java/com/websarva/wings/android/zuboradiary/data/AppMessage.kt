package com.websarva.wings.android.zuboradiary.data

import android.content.Context
import com.websarva.wings.android.zuboradiary.R

enum class AppMessage(
    private val dialogTitleStringResId: Int,
    private val dialogMessageStringResId: Int
) {
    DIARY_LOADING_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_diary_loading_error
    ),
    DIARY_SAVING_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_diary_saving_error
    ),
    DIARY_DELETE_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_diary_delete_error
    ),
    DIARY_INFO_LOADING_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_diary_information_loading_error
    ),
    DIARY_ITEM_TITLE_HISTORY_LOADING_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_item_title_selection_history_loading_error
    ),
    DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_item_title_selection_history_item_delete_error
    ),
    SETTING_LOADING_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_setting_loading_error
    ),
    SETTING_UPDATE_ERROR(
        R.string.enum_app_message_title_access_error,
        R.string.enum_app_message_message_setting_update_error
    ),
    WEATHER_INFO_LOADING_ERROR(
        R.string.enum_app_message_title_connection_error,
        R.string.enum_app_message_message_weather_information_loading_error
    );

    fun getDialogTitle(context: Context): String {
        return context.getString(dialogTitleStringResId)
    }

    fun getDialogMessage(context: Context): String {
        return context.getString(dialogMessageStringResId)
    }
}
