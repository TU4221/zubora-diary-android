package com.websarva.wings.android.zuboradiary.ui.theme

import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.ui.utils.themeResId
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

/**
 * 指定されたテーマカラーを持つ新しいLayoutInflaterを生成する。
 *
 * @param themeColor 適用するテーマカラー。
 * @return テーマが適用された新しいLayoutInflaterインスタンス。
 */
internal fun LayoutInflater.withTheme(themeColor: ThemeColorUi): LayoutInflater {
    val themedContext = ContextThemeWrapper(context, themeColor.themeResId)
    return cloneInContext(themedContext)
}
