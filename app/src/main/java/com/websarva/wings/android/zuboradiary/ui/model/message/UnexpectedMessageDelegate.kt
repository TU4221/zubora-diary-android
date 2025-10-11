package com.websarva.wings.android.zuboradiary.ui.model.message

import com.squareup.leakcanary.core.BuildConfig
import com.websarva.wings.android.zuboradiary.R

internal class UnexpectedMessageDelegate(val exception: Exception) {
    val dialogTitleStringResId: Int = R.string.dialog_app_message_title_unexpected_error
    val dialogMessageStringResId: Int =
        if (BuildConfig.DEBUG) {
            R.string.dialog_app_message_developer_unexpected_error
        } else {
            R.string.dialog_app_message_unexpected_error
        }
    val dialogMessageArgs: List<Any> =
        if (BuildConfig.DEBUG) {
            listOf(exception.javaClass.simpleName)
        } else {
            emptyList()
        }
}
