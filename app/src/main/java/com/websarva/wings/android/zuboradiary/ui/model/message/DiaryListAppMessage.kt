package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class DiaryListAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryListLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_list_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryListLoadFailure
    }

    data object DiaryDeleteFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryImageDeleteFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_image_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryImageDeleteFailure
    }

    data object DiaryInfoLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_info_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }
}
