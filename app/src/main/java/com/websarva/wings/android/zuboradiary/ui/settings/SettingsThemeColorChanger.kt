package com.websarva.wings.android.zuboradiary.ui.settings

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.common.theme.asOnSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.common.theme.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.common.theme.asSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorChanger

/**
 * 設定画面のViewに特化したテーマカラーを動的に変更するためのヘルパークラス。
 *
 * このクラスは、[ThemeColorChanger]を継承し、設定項目のセクションヘッダーやアイコンなどの配色を適用する責務を持つ。
 */
internal class SettingsThemeColorChanger : ThemeColorChanger() {

    /**
     * 複数の設定項目のセクションヘッダー（[android.widget.TextView]）の背景とテキストの色を適用する。
     * @param textViewList 対象となるセクションヘッダーのリスト。
     * @param themeColor 適用するテーマカラー。
     */
    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().resources

        val color = themeColor.asSecondaryContainerColorInt(resources)
        val onColor = themeColor.asOnSecondaryContainerColorInt(resources)
        applyTextViewsColor(textViewList, color, onColor, null)
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
        applyTextViewsColor(textViewList, null, null, color)
    }
}
