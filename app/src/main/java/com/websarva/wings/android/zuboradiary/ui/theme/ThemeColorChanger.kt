package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.materialswitch.MaterialSwitch
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import java.util.function.Consumer

/**
 * Enum ThemeColorをもとにViewの色を変更するクラス。
 * Activity、各Fragment共通処理を本クラスにまとめる。
 * 各Fragment固有のViewに対しては本クラスを継承して継承クラスにメソッドを追加する。
 * 本クラスに記述されている各Viewの色はアプリ背景色(SurfaceColor)を考慮して選定。
 */
internal open class ThemeColorChanger {

    protected fun View.requireResources(): Resources {
        return requireNotNull(context.resources)
    }

    fun applyBackgroundColor(view: View, themeColor: ThemeColor) {
        val resources = view.requireResources()
        val surfaceColor = themeColor.getSurfaceColor(resources)
        applyViewColor(view, surfaceColor)
    }

    fun applyTextColorOnBackground(textViewList: List<TextView>, themeColor: ThemeColor) {
        require(textViewList.isNotEmpty())

        val resources = textViewList.first().requireResources()
        val onSurfaceColor = themeColor.getOnSurfaceColor(resources)
        applyTextViewsColorOnlyText(textViewList, onSurfaceColor)
    }

    fun applyRedTextColorOnBackground(textViewList: List<TextView>, themeColor: ThemeColor) {
        require(textViewList.isNotEmpty())

        val resources = textViewList.first().requireResources()
        val onSurfaceColor = themeColor.getErrorColor(resources)
        applyTextViewsColorOnlyText(textViewList, onSurfaceColor)
    }

    fun applyStatusBarColor(window: Window, themeColor: ThemeColor) {
        // ステータスバーのアイコンの色を変更(白 or 灰)
        val isLight = themeColor.isAppearanceLightStatusBars
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    fun applyNavigationBarColor(window: Window, themeColor: ThemeColor) {
        // ナビゲエーションバーのアイコンの色を変更
        val isLight = themeColor.isAppearanceLightStatusBars
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isLight
    }

    fun applyBottomNavigationColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        applyBottomNavigationBackgroundColor(bottomNavigationView, themeColor)
        applyBottomNavigationItemRippleColor(bottomNavigationView, themeColor)
        applyBottomNavigationItemTextColor(bottomNavigationView, themeColor)
        applyBottomNavigationItemIconColor(bottomNavigationView, themeColor)
        applyBottomNavigationActiveIndicatorColor(bottomNavigationView, themeColor)
    }

    private fun applyBottomNavigationBackgroundColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        val resources = bottomNavigationView.requireResources()

        val color = themeColor.getSurfaceContainerColor(resources)
        bottomNavigationView.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun applyBottomNavigationItemRippleColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        val resources = bottomNavigationView.requireResources()

        val checkedColor = themeColor.getPrimaryColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemRippleColor = colorStateList
    }

    private fun applyBottomNavigationItemTextColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        val resources = bottomNavigationView.requireResources()

        val checkedColor = themeColor.getOnSurfaceColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemTextColor = colorStateList
    }

    private fun applyBottomNavigationItemIconColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        val resources = bottomNavigationView.requireResources()

        val checkedColor = themeColor.getOnSecondaryContainerColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemIconTintList = colorStateList
    }

    private fun applyBottomNavigationActiveIndicatorColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColor
    ) {
        val resources = bottomNavigationView.requireResources()

        val secondaryContainerColor = themeColor.getSecondaryContainerColor(resources)
        bottomNavigationView.itemActiveIndicatorColor =
            ColorStateList.valueOf(secondaryContainerColor)
    }

    fun applyToolbarColor(toolbar: MaterialToolbar, themeColor: ThemeColor) {
        val resources = toolbar.requireResources()

        val surfaceColor = themeColor.getSurfaceColor(resources)
        val onSurfaceColor = themeColor.getOnSurfaceColor(resources)
        val onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources)
        toolbar.setBackgroundColor(surfaceColor)
        toolbar.setTitleTextColor(onSurfaceColor)
        applyToolbarMenuColor(toolbar, onSurfaceColor)
        applyToolbarNavigationIconColor(toolbar, onSurfaceVariantColor)
    }

    private fun applyToolbarNavigationIconColor(toolbar: MaterialToolbar, color: Int) {
        val navigationIcon = toolbar.navigationIcon ?: return
        navigationIcon.setTint(color)
    }

    private fun applyToolbarMenuColor(toolbar: MaterialToolbar, color: Int) {
        val menuIcon = toolbar.overflowIcon
        menuIcon?.setTint(color)

        val collapseIcon = toolbar.collapseIcon
        collapseIcon?.setTint(color)

        applyToolbarMenuIconColor(toolbar, color)
    }

    private fun applyToolbarMenuIconColor(toolbar: MaterialToolbar, color: Int) {
        val menu = toolbar.menu ?: return

        val numMenuIcons = menu.size()
        if (numMenuIcons <= 0) return

        for (i in 0 until numMenuIcons) {
            val icon = menu.getItem(i).icon
            icon?.setTint(color)
        }
    }

    fun applySwitchColor(switchList: List<MaterialSwitch>, themeColor: ThemeColor) {
        applySwitchThumbColor(switchList, themeColor)
        applySwitchThumbIconColor(switchList, themeColor)
        applySwitchTrackColor(switchList, themeColor)
    }

    private fun applySwitchThumbColor(switchList: List<MaterialSwitch>, themeColor: ThemeColor) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().requireResources()

        val checkedColor = themeColor.getOnPrimaryColor(resources)
        val unCheckedColor = themeColor.getOutlineColor(resources)
        val thumbColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbTintList = thumbColorStateList
        })
    }

    private fun applySwitchThumbIconColor(switchList: List<MaterialSwitch>, themeColor: ThemeColor) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().requireResources()

        val checkedColor = themeColor.getOnPrimaryContainerColor(resources)
        val unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources)
        val thumbIconColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbIconTintList = thumbIconColorStateList
        })
    }

    private fun applySwitchTrackColor(switchList: List<MaterialSwitch>, themeColor: ThemeColor) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().requireResources()

        val checkedColor = themeColor.getPrimaryColor(resources)
        val unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources)
        val trackColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.trackTintList = trackColorStateList
        })
    }

    fun applyDividerColor(dividerList: List<MaterialDivider>, themeColor: ThemeColor) {
        require(dividerList.isNotEmpty())
        val resources = dividerList.first().requireResources()

        val color = themeColor.getOutlineVariantColor(resources)
        dividerList.forEach(Consumer { x: MaterialDivider ->
            x.dividerColor = color
        })
    }

    // 共通処理
    protected open fun applyViewColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    protected open fun applyTextViewsColor(textViewList: List<TextView>, color: Int, onColor: Int) {
        textViewList.forEach(Consumer { x: TextView -> applyTextViewColor(x, color, onColor) })
    }

    protected open fun applyTextViewColor(textView: TextView, color: Int, onColor: Int) {
        textView.setBackgroundColor(color)
        textView.setTextColor(onColor)
    }

    protected open fun applyTextViewsColorOnlyText(textViewList: List<TextView>, onColor: Int) {
        textViewList.forEach(Consumer { x: TextView -> applyTextViewColorOnlyText(x, onColor) })
    }

    protected open fun applyTextViewColorOnlyText(textView: TextView, color: Int) {
        textView.setTextColor(color)
    }

    protected open fun applyTextViewsColorOnlyIcon(textViewList: List<TextView>, color: Int) {
        textViewList.forEach(Consumer { x: TextView -> applyTextViewColorOnlyIcon(x, color) })
    }

    protected open fun applyTextViewColorOnlyIcon(view: TextView, color: Int) {
        val drawables = view.compoundDrawablesRelative
        val wrappedDrawable = arrayOfNulls<Drawable>(drawables.size)

        for (i in drawables.indices) {
            val drawable = drawables[i]
            if (drawable != null) {
                wrappedDrawable[i] = DrawableCompat.wrap(drawable)
                DrawableCompat.setTint(checkNotNull(wrappedDrawable[i]) , color)
            }
        }
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
            wrappedDrawable[0], wrappedDrawable[1], wrappedDrawable[2], wrappedDrawable[3]
        )
    }

    protected open fun applyDrawableColor(drawable: Drawable, color: Int) {
        drawable.setTint(color)
    }

    protected open fun createCheckedColorStateList(checkedColor: Int, unCheckedColor: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // ON状態
            intArrayOf(-android.R.attr.state_checked) // OFF状態
        )
        val colors = intArrayOf(
            checkedColor,
            unCheckedColor
        )

        return ColorStateList(states, colors)
    }
}
