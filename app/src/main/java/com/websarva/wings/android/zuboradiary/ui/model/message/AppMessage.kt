package com.websarva.wings.android.zuboradiary.ui.model.message

import android.content.Context
import java.io.Serializable

internal abstract class AppMessage(
    private val dialogTitleStringResId: Int,
    private val dialogMessageStringResId: Int
) : Serializable {
    fun getDialogTitle(context: Context): String {
        return context.getString(dialogTitleStringResId)
    }

    fun getDialogMessage(context: Context): String {
        return context.getString(dialogMessageStringResId)
    }
}
