package com.websarva.wings.android.zuboradiary.ui.theme

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

/**
 * 設定画面のViewに特化したテーマカラーを動的に変更するためのヘルパークラス。
 *
 * このクラスは、[ThemeColorChanger]を継承し、設定項目のセクションヘッダーやアイコンなどの配色を適用する責務を持つ。
 */
internal class SettingsThemeColorChanger : ThemeColorChanger() {

    /**
     * 複数の設定項目のセクションヘッダー（[TextView]）の背景とテキストの色を適用する。
     * @param textViewList 対象となるセクションヘッダーのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().resources

        val color = themeColor.asSecondaryContainerColorInt(resources)
        val onColor = themeColor.asOnSecondaryContainerColorInt(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    /**
     * 複数の設定項目のアイコン（Compound Drawable）の色を適用する。
     * @param textViewList 対象となるアイコンを持つTextViewのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applySettingItemIconColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().resources

        val color = themeColor.asOnSurfaceVariantColorInt(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
