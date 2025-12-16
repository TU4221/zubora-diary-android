package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.sheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な複数選択ピッカーダイアログへの遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property pickerConfigs 各NumberPickerの設定を定義するリスト。このリストのサイズが表示されるピッカーの数となる（最大3つ）。
 */
@Parcelize
data class ListPickersDialogParams(
    val resultKey: String,
    val pickerConfigs: List<ListPickerConfig>
) : Parcelable
