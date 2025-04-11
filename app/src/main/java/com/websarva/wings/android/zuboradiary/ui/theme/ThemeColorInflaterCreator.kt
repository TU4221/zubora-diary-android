package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor

internal class ThemeColorInflaterCreator(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val themeColor: ThemeColor) {

    fun create(): LayoutInflater {
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(context, themeResId)
        return inflater.cloneInContext(contextWithTheme)
    }
}
