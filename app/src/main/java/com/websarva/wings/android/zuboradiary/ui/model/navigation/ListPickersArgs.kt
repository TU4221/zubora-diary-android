package com.websarva.wings.android.zuboradiary.ui.model.navigation

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.ListPickersDialogFragment
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な複数選択ピッカーダイアログ（[ListPickersDialogFragment]）に渡すための引数モデル。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property pickerConfigs 各NumberPickerの設定を定義するリスト。このリストのサイズが表示されるピッカーの数となる（最大3つ）。
 */
@Parcelize
data class ListPickersArgs(
    val resultKey: String,
    val pickerConfigs: List<ListPickerConfig>
) : Parcelable
