package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.ui.utils.themeResId
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

internal class ThemeColorInflaterCreator {

    fun create(inflater: LayoutInflater, themeColor: ThemeColorUi): LayoutInflater {
        val context = inflater.context
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(context, themeResId)

        return inflater.cloneInContext(contextWithTheme)
    }
}
