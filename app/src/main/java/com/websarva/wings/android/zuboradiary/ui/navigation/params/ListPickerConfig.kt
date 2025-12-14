package com.websarva.wings.android.zuboradiary.ui.navigation.params

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 個々のNumberPickerの設定を定義するモデル。
 *
 * @property items 表示する文字列のリスト。
 * @property initialIndex 初期選択位置のインデックス。
 */
@Parcelize
data class ListPickerConfig(
    val items: List<String>,
    val initialIndex: Int = 0
) : Parcelable
