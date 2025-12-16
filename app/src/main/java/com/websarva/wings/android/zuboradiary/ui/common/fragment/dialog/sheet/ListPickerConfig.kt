package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な複数選択ピッカーダイアログ（[ListPickersDialogFragment]）の個々のNumberPickerの設定データ。
 *
 * @property items 表示する文字列のリスト。
 * @property initialIndex 初期選択位置のインデックス。
 */
@Parcelize
data class ListPickerConfig(
    val items: List<String>,
    val initialIndex: Int = 0
) : Parcelable
