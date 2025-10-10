package com.websarva.wings.android.zuboradiary.ui.model.message

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes

internal sealed interface AppMessage : Parcelable {

    @get:StringRes
    val dialogTitleStringResId: Int

    @get:StringRes
    val dialogMessageStringResId: Int
    
    val dialogMessageArgs: List<Any>
        get() = emptyList()

    fun getDialogTitle(context: Context): String {
        return context.getString(dialogTitleStringResId)
    }

    fun getDialogMessage(context: Context): String {
        return context.getString(dialogMessageStringResId, dialogMessageArgs.toTypedArray())
    }
}
