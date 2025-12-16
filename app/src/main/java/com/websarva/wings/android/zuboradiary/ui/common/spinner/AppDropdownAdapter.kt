package com.websarva.wings.android.zuboradiary.ui.common.spinner

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.theme.themeResId
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi

/**
 * アプリケーション共通で利用する、テーマカラー対応のドロップダウンメニュー用アダプター。
 *
 * [AutoCompleteTextView]などに設定することで、選択されたテーマカラーに基づいたデザインの
 * ドロップダウンリストを表示する。
 *
 * @param context コンテキスト
 * @param themeColor 適用するテーマカラー
 * @param objects 表示する文字列のリスト
 */
internal class AppDropdownAdapter(
    context: Context,
    themeColor: ThemeColorUi,
    objects: List<String>
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    objects
)
