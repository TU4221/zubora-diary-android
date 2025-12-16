package com.websarva.wings.android.zuboradiary.ui.common.theme

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.function.Consumer
import androidx.core.view.size
import androidx.core.view.get

/**
 * UIコンポーネントのテーマカラーを動的に変更するためのヘルパークラス。
 *
 * このクラスは、[ThemeColorUi]に基づき、ステータスバー、ツールバー、ボトムナビゲーションビューなど、
 * アプリケーション内の様々なViewの配色を適用する責務を持つ。
 */
internal open class ThemeColorChanger {

    // region System Bars
    /**
     * ステータスバーのアイコンの色（明/暗）をテーマカラーに応じて切り替える。
     * @param window 対象のWindow。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyStatusBarIconColor(window: Window, themeColor: ThemeColorUi) {
        val isLight = themeColor.isAppearanceLightStatusBars
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    /**
     * ナビゲーションバーのアイコンの色（明/暗）をテーマカラーに応じて切り替える。
     * @param window 対象のWindow。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyNavigationBarIconColor(window: Window, themeColor: ThemeColorUi) {
        val isLight = themeColor.isAppearanceLightStatusBars
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isLight
    }
    // endregion

    // region App Main View
    /**
     * [View]の背景色にアプリケーションの基本的な背景色を適用する。
     * @param view 対象のView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppBackgroundColor(view: View, themeColor: ThemeColorUi) {
        val resources = view.resources
        val surfaceColor = themeColor.asSurfaceColorInt(resources)
        applyViewColor(view, surfaceColor)
    }

    /**
     * 複数の[TextView]に、アプリケーションの基本的な背景に対するテキストカラーを適用する。
     * @param textViewList 対象のTextViewのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppTextColorOnBackground(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())

        val resources = textViewList.first().resources
        val onSurfaceColor = themeColor.asOnSurfaceColorInt(resources)
        applyTextViewsColor(textViewList, null, onSurfaceColor, null)
    }

    /**
     * 複数の[TextView]に、アプリケーションの基本的なエラーを示す色のテキストカラーを適用する。
     * @param textViewList 対象のTextViewのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppTextErrorColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())

        val resources = textViewList.first().resources
        val errorColor = themeColor.asErrorColorInt(resources)
        applyTextViewsColor(textViewList, null, errorColor, null)
    }

    /**
     * 複数の[MaterialDivider]にアプリケーションの基本的な色を適用する。
     * @param dividerList 対象のMaterialDividerのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppDividerColor(dividerList: List<MaterialDivider>, themeColor: ThemeColorUi) {
        require(dividerList.isNotEmpty())
        val resources = dividerList.first().resources

        val color = themeColor.asOutlineVariantColorInt(resources)
        dividerList.forEach(Consumer { x: MaterialDivider ->
            x.dividerColor = color
        })
    }
    // endregion

    // region Bottom Navigation View
    /**
     * [BottomNavigationView]の全ての配色（背景、波紋、テキスト、アイコン、アクティブインジケータ）を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppBottomNavigationColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        applyAppBottomNavigationBackgroundColor(bottomNavigationView, themeColor)
        applyAppBottomNavigationItemRippleColor(bottomNavigationView, themeColor)
        applyAppBottomNavigationItemTextColor(bottomNavigationView, themeColor)
        applyAppBottomNavigationItemIconColor(bottomNavigationView, themeColor)
        applyAppBottomNavigationActiveIndicatorColor(bottomNavigationView, themeColor)
    }

    /**
     * [applyAppBottomNavigationColor]から呼び出され、背景色を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppBottomNavigationBackgroundColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        val resources = bottomNavigationView.resources

        val color = themeColor.asSurfaceContainerColorInt(resources)
        bottomNavigationView.backgroundTintList = ColorStateList.valueOf(color)
    }

    /**
     * [applyAppBottomNavigationColor]から呼び出され、アイテムの波紋色を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppBottomNavigationItemRippleColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        val resources = bottomNavigationView.resources

        val checkedColor = themeColor.asPrimaryColorInt(resources)
        val unCheckedColor = themeColor.asOnSurfaceVariantColorInt(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemRippleColor = colorStateList
    }

    /**
     * [applyAppBottomNavigationColor]から呼び出され、アイテムのテキスト色を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppBottomNavigationItemTextColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        val resources = bottomNavigationView.resources

        val checkedColor = themeColor.asOnSurfaceColorInt(resources)
        val unCheckedColor = themeColor.asOnSurfaceVariantColorInt(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemTextColor = colorStateList
    }

    /**
     * [applyAppBottomNavigationColor]から呼び出され、アイテムのアイコン色を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppBottomNavigationItemIconColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        val resources = bottomNavigationView.resources

        val checkedColor = themeColor.asOnSecondaryContainerColorInt(resources)
        val unCheckedColor = themeColor.asOnSurfaceVariantColorInt(resources)
        val colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        bottomNavigationView.itemIconTintList = colorStateList
    }

    /**
     * [applyAppBottomNavigationColor]から呼び出され、アクティブなアイテムのインジケータ色を適用する。
     * @param bottomNavigationView 対象のBottomNavigationView。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppBottomNavigationActiveIndicatorColor(
        bottomNavigationView: BottomNavigationView,
        themeColor: ThemeColorUi
    ) {
        val resources = bottomNavigationView.resources

        val secondaryContainerColor = themeColor.asSecondaryContainerColorInt(resources)
        bottomNavigationView.itemActiveIndicatorColor =
            ColorStateList.valueOf(secondaryContainerColor)
    }
    // endregion

    // region Toolbar
    /**
     * [MaterialToolbar]の配色（背景、タイトル、メニューアイコン、ナビゲーションアイコン）を適用する。
     * @param toolbar 対象のMaterialToolbar。
     * @param themeColor 適用するテーマカラー。
     * @param appBarLayout ツールバーを含むAppBarLayout（任意）。指定された場合、背景色が適用される。
     */
    fun applyAppToolbarColor(
        toolbar: MaterialToolbar,
        themeColor: ThemeColorUi,
        appBarLayout: AppBarLayout? = null
    ) {
        val resources = toolbar.resources

        val surfaceColor = themeColor.asSurfaceColorInt(resources)
        val onSurfaceColor = themeColor.asOnSurfaceColorInt(resources)
        val onSurfaceVariantColor = themeColor.asOnSurfaceVariantColorInt(resources)
        toolbar.setBackgroundColor(surfaceColor)
        toolbar.setTitleTextColor(onSurfaceColor)
        applyAppToolbarMenuColor(toolbar, onSurfaceColor)
        applyAppToolbarNavigationIconColor(toolbar, onSurfaceVariantColor)

        appBarLayout?.setBackgroundColor(surfaceColor)
    }

    /**
     * [applyAppToolbarColor]から呼び出され、ナビゲーションアイコンの色を適用する。
     * @param toolbar 対象のMaterialToolbar。
     * @param color 適用する色。
     */
    private fun applyAppToolbarNavigationIconColor(toolbar: MaterialToolbar, color: Int) {
        toolbar.navigationIcon?.setTint(color)
    }

    /**
     * [applyAppToolbarColor]から呼び出され、メニュー関連のアイコン（オーバーフロー、折りたたみ）の色を適用する。
     * @param toolbar 対象のMaterialToolbar。
     * @param color 適用する色。
     */
    private fun applyAppToolbarMenuColor(toolbar: MaterialToolbar, color: Int) {
        val menuIcon = toolbar.overflowIcon
        menuIcon?.setTint(color)

        val collapseIcon = toolbar.collapseIcon
        collapseIcon?.setTint(color)

        applyAppToolbarMenuIconColor(toolbar, color)
    }

    /**
     * [applyAppToolbarMenuColor]から呼び出され、ツールバーのメニューアイテムに含まれる各アイコンの色を適用する。
     * @param toolbar 対象のMaterialToolbar。
     * @param color 適用する色。
     */
    private fun applyAppToolbarMenuIconColor(toolbar: MaterialToolbar, color: Int) {
        toolbar.menu?.let {
            val numMenuIcons = it.size
            if (numMenuIcons <= 0) return

            for (i in 0 until numMenuIcons) {
                val icon = it[i].icon
                icon?.setTint(color)
            }
        }
    }
    // endregion

    // region Switch
    /**
     * [MaterialSwitch]の全ての配色（つまみ、つまみのアイコン、トラック）を適用する。
     * @param switchList 対象のMaterialSwitchのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyAppSwitchColor(switchList: List<MaterialSwitch>, themeColor: ThemeColorUi) {
        applyAppSwitchThumbColor(switchList, themeColor)
        applyAppSwitchThumbIconColor(switchList, themeColor)
        applyAppSwitchTrackColor(switchList, themeColor)
    }

    /**
     * [applyAppSwitchColor]から呼び出され、スイッチのつまみの色を適用する。
     * @param switchList 対象のMaterialSwitchのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppSwitchThumbColor(switchList: List<MaterialSwitch>, themeColor: ThemeColorUi) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().resources

        val checkedColor = themeColor.asOnPrimaryColorInt(resources)
        val unCheckedColor = themeColor.asOutlineColorInt(resources)
        val thumbColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbTintList = thumbColorStateList
        })
    }

    /**
     * [applyAppSwitchColor]から呼び出され、スイッチのつまみアイコンの色を適用する。
     * @param switchList 対象のMaterialSwitchのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppSwitchThumbIconColor(switchList: List<MaterialSwitch>, themeColor: ThemeColorUi) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().resources

        val checkedColor = themeColor.asOnPrimaryContainerColorInt(resources)
        val unCheckedColor = themeColor.asSurfaceContainerHighestColorInt(resources)
        val thumbIconColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.thumbIconTintList = thumbIconColorStateList
        })
    }

    /**
     * [applyAppSwitchColor]から呼び出され、スイッチのトラックの色を適用する。
     * @param switchList 対象のMaterialSwitchのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    private fun applyAppSwitchTrackColor(switchList: List<MaterialSwitch>, themeColor: ThemeColorUi) {
        require(switchList.isNotEmpty())
        val resources = switchList.first().resources

        val checkedColor = themeColor.asPrimaryColorInt(resources)
        val unCheckedColor = themeColor.asSurfaceContainerHighestColorInt(resources)
        val trackColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor)
        switchList.forEach(Consumer { x: MaterialSwitch ->
            x.trackTintList = trackColorStateList
        })
    }
    // endregion

    // region Helper
    /**
     * [View]の背景色を設定する共通ヘルパーメソッド。
     * @param view 対象のView。
     * @param color 適用する色。
     */
    protected fun applyViewColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    /**
     * 複数の[TextView]に色を適用する。
     * @param textViewList 対象のTextViewのリスト。
     * @param backgroundColor 背景色として適用する色。nullの場合は変更しない。
     * @param textColor テキスト色として適用する色。nullの場合は変更しない。
     * @param iconColor Compound Drawable（アイコン）の色として適用する色。nullの場合は変更しない。
     */
    protected fun applyTextViewsColor(
        textViewList: List<TextView>,
        backgroundColor: Int? = null,
        textColor: Int? = null,
        iconColor: Int? = null
    ) {
        if (backgroundColor != null || textColor != null || iconColor != null) {
            textViewList.forEach { textView ->
                applyTextViewColor(textView, backgroundColor, textColor, iconColor)
            }
        }
    }

    /**
     * 単一の[TextView]に色を適用する。
     *
     * @param textView 対象のTextView。
     * @param backgroundColor 背景色として適用する色。nullの場合は変更しない。
     * @param textColor テキスト色として適用する色。nullの場合は変更しない。
     * @param iconColor Compound Drawable（アイコン）の色として適用する色。nullの場合は変更しない。
     */
    protected fun applyTextViewColor(
        textView: TextView,
        backgroundColor: Int? = null,
        textColor: Int? = null,
        iconColor: Int? = null
    ) {
        backgroundColor?.let { textView.setBackgroundColor(it) }
        textColor?.let { textView.setTextColor(it) }
        iconColor?.let {
            val drawables = textView.compoundDrawablesRelative
            val wrappedDrawable = arrayOfNulls<Drawable>(drawables.size)

            for (i in drawables.indices) {
                val drawable = drawables[i]
                if (drawable != null) {
                    wrappedDrawable[i] = DrawableCompat.wrap(drawable).mutate()
                    DrawableCompat.setTint(checkNotNull(wrappedDrawable[i]), it)
                }
            }
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                wrappedDrawable[0], wrappedDrawable[1], wrappedDrawable[2], wrappedDrawable[3]
            )
        }
    }

    /**
     * [Drawable]の色合いを設定する共通ヘルパーメソッド。
     * @param drawable 対象のDrawable。
     * @param color 適用する色。
     */
    protected fun applyDrawableColor(drawable: Drawable, color: Int) {
        drawable.setTint(color)
    }

    /**
     * ON/OFFの状態（[android.R.attr.state_checked]）に応じて
     * 色を切り替える[ColorStateList]を生成する共通ヘルパーメソッド。
     * @param checkedColor ON状態の色。
     * @param unCheckedColor OFF状態の色。
     * @return 生成されたColorStateList。
     */
    protected fun createCheckedColorStateList(checkedColor: Int, unCheckedColor: Int): ColorStateList {
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
    // endregion
}
