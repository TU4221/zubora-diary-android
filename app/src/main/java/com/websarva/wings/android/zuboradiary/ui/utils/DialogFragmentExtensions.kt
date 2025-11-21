package com.websarva.wings.android.zuboradiary.ui.utils

import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger

/**
 * [DialogFragment]でエッジ・ツー・エッジ表示を有効にするための拡張関数。
 * @param themeColor テーマに応じてナビゲーションバーのアイコンカラーなどを設定するために使用する。
 */
internal fun DialogFragment.enableEdgeToEdge(themeColor: ThemeColorUi) {
    val window = dialog?.window ?: return
    setupEdgeToEdge(window)
    setupStatusBarAndNavigationBarIconColor(window, themeColor)
}

/**
 * 指定されたWindowでエッジ・ツー・エッジ表示をセットアップする。
 * @param window セットアップ対象のWindow。
 */
private fun setupEdgeToEdge(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
}

/**
 * ナビゲーションバーのアイコンの色をテーマカラーに応じて切り替える。
 * @param window 対象のWindow。
 * @param themeColor 適用するテーマカラー。
 */
private fun setupStatusBarAndNavigationBarIconColor(window: Window, themeColor: ThemeColorUi) {
    val changer = ThemeColorChanger()
    changer.applyNavigationBarIconColor(window, themeColor)
}
