package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な複数選択ピッカーダイアログ（[ListPickersDialogFragment]）からの結果データ。
 *
 * @property firstPickerValue 1番目のピッカーで選択された値（インデックス）。
 * @property secondPickerValue 2番目のピッカーで選択された値。表示されていない場合はnull。
 * @property thirdPickerValue 3番目のピッカーで選択された値。表示されていない場合はnull。
 */
@Parcelize
data class ListPickersDialogResult(
    val firstPickerValue: Int,
    val secondPickerValue: Int?,
    val thirdPickerValue: Int?
) : Parcelable
