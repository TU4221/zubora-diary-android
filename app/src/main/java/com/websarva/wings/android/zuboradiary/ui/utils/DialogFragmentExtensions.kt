package com.websarva.wings.android.zuboradiary.ui.utils

import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger

internal fun DialogFragment.enableEdgeToEdge(themeColor: ThemeColorUi) {
    val window = dialog?.window ?: return
    setUpEdgeToEdge(window)
    setUpStatusBarAndNavigationBarIconColor(window, themeColor)
}

private fun setUpEdgeToEdge(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
}

private fun setUpStatusBarAndNavigationBarIconColor(window: Window, themeColor: ThemeColorUi) {
    val changer = ThemeColorChanger()
    changer.applyNavigationBarIconColor(window, themeColor)
}
