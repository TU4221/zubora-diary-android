package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
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
internal open class ThemeColorSwitcher(protected val context: Context, protected val themeColor: ThemeColor) {

    protected val resources: Resources = context.resources

    fun switchBackgroundColor(view: View) {
        val surfaceColor = themeColor.getSurfaceColor(resources)
        switchViewColor(view, surfaceColor)
    }

    fun switchTextColorOnBackground(textViewList: List<TextView>) {
        val onSurfaceColor = themeColor.getOnSurfaceColor(resources)
        switchTextViewsColorOnlyText(textViewList, onSurfaceColor)
    }

    fun switchRedTextColorOnBackground(textViewList: List<TextView>) {
        val onSurfaceColor = themeColor.getErrorColor(resources)
        switchTextViewsColorOnlyText(textViewList, onSurfaceColor)
    }

    fun switchStatusBarColor(window: Window) {
        // ステータスバーのアイコンの色を変更(白 or 灰)
        val isLight = themeColor.isAppearanceLightStatusBars
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    fun switchBottomNavigationColor(bottomNavigationView: BottomNavigationView) {
        switchBottomNavigationBackgroundColor(bottomNavigationView)
        switchBottomNavigationItemRippleColor(bottomNavigationView)
        switchBottomNavigationItemTextColor(bottomNavigationView)
        switchBottomNavigationItemIconColor(bottomNavigationView)
        switchBottomNavigationActiveIndicatorColor(bottomNavigationView)
    }

    private fun switchBottomNavigationBackgroundColor(bottomNavigationView: BottomNavigationView) {
        val color = themeColor.getSurfaceContainerColor(resources)
        bottomNavigationView.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun switchBottomNavigationItemRippleColor(bottomNavigationView: BottomNavigationView) {
        val checkedColor = themeColor.getPrimaryColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemRippleColor = colorStateList
    }

    private fun switchBottomNavigationItemTextColor(bottomNavigationView: BottomNavigationView) {
        val checkedColor = themeColor.getOnSurfaceColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemTextColor = colorStateList
    }

    private fun switchBottomNavigationItemIconColor(bottomNavigationView: BottomNavigationView) {
        val checkedColor = themeColor.getOnSecondaryContainerColor(resources)
        val unCheckedColor = themeColor.getOnSurfaceVariantColor(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemIconTintList = colorStateList
    }

    private fun switchBottomNavigationActiveIndicatorColor(bottomNavigationView: BottomNavigationView) {
        val secondaryContainerColor = themeColor.getSecondaryContainerColor(resources)
        bottomNavigationView.itemActiveIndicatorColor =
            ColorStateList.valueOf(secondaryContainerColor)
    }

    fun switchToolbarColor(toolbar: MaterialToolbar) {
        val surfaceColor = themeColor.getSurfaceColor(resources)
        val onSurfaceColor = themeColor.getOnSurfaceColor(resources)
        val onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources)
        toolbar.setBackgroundColor(surfaceColor)
        toolbar.setTitleTextColor(onSurfaceColor)
        switchToolbarMenuColor(toolbar, onSurfaceColor)
        switchToolbarNavigationIconColor(toolbar, onSurfaceVariantColor)
    }

    private fun switchToolbarNavigationIconColor(toolbar: MaterialToolbar, color: Int) {
        val navigationIcon = toolbar.navigationIcon ?: return
        navigationIcon.setTint(color)
    }

    private fun switchToolbarMenuColor(toolbar: MaterialToolbar, color: Int) {
        val menuIcon = toolbar.overflowIcon
        menuIcon?.setTint(color)

        val collapseIcon = toolbar.collapseIcon
        collapseIcon?.setTint(color)

        switchToolbarMenuIconColor(toolbar, color)
    }

    private fun switchToolbarMenuIconColor(toolbar: MaterialToolbar, color: Int) {
        val menu = toolbar.menu ?: return

        val numMenuIcons = menu.size()
        if (numMenuIcons <= 0) return

        for (i in 0 until numMenuIcons) {
            val icon = menu.getItem(i).icon
            icon?.setTint(color)
        }
    }

    fun switchSwitchColor(switchList: List<MaterialSwitch>) {
        switchSwitchThumbColor(switchList)
        switchSwitchThumbIconColor(switchList)
        switchSwitchTrackColor(switchList)
    }

    private fun switchSwitchThumbColor(switchList: List<MaterialSwitch>) {
        val checkedColor = themeColor.getOnPrimaryColor(resources)
        val unCheckedColor = themeColor.getOutlineColor(resources)
        val thumbColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbTintList = thumbColorStateList
        })
    }

    private fun switchSwitchThumbIconColor(switchList: List<MaterialSwitch>) {
        val checkedColor = themeColor.getOnPrimaryContainerColor(resources)
        val unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources)
        val thumbIconColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbIconTintList = thumbIconColorStateList
        })
    }

    private fun switchSwitchTrackColor(switchList: List<MaterialSwitch>) {
        val checkedColor = themeColor.getPrimaryColor(resources)
        val unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources)
        val trackColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.trackTintList = trackColorStateList
        })
    }

    fun switchDividerColor(dividerList: List<MaterialDivider>) {
        val color = themeColor.getOutlineVariantColor(resources)
        dividerList.forEach(Consumer { x: MaterialDivider ->
            x.dividerColor = color
        })
    }

    // 共通処理
    protected open fun switchViewColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    protected open fun switchTextViewsColor(textViewList: List<TextView>, color: Int, onColor: Int) {
        textViewList.forEach(Consumer { x: TextView -> switchTextViewColor(x, color, onColor) })
    }

    protected open fun switchTextViewColor(textView: TextView, color: Int, onColor: Int) {
        textView.setBackgroundColor(color)
        textView.setTextColor(onColor)
    }

    protected open fun switchTextViewsColorOnlyText(textViewList: List<TextView>, onColor: Int) {
        textViewList.forEach(Consumer { x: TextView -> switchTextViewColorOnlyText(x, onColor) })
    }

    protected open fun switchTextViewColorOnlyText(textView: TextView, color: Int) {
        textView.setTextColor(color)
    }

    protected open fun switchTextViewsColorOnlyIcon(textViewList: List<TextView>, color: Int) {
        textViewList.forEach(Consumer { x: TextView -> switchTextViewColorOnlyIcon(x, color) })
    }

    protected open fun switchTextViewColorOnlyIcon(view: TextView, color: Int) {
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

    protected open fun switchDrawableColor(drawable: Drawable, color: Int) {
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
