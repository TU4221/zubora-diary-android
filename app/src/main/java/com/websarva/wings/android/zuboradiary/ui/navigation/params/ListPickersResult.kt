package com.websarva.wings.android.zuboradiary.ui.navigation.params

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.ListPickersDialogFragment
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な複数選択ピッカーダイアログ（[ListPickersDialogFragment]）からの結果を表現するモデル。
 *
 * @property firstPickerValue 1番目のピッカーで選択された値（インデックス）。
 * @property secondPickerValue 2番目のピッカーで選択された値。表示されていない場合はnull。
 * @property thirdPickerValue 3番目のピッカーで選択された値。表示されていない場合はnull。
 */
@Parcelize
data class ListPickersResult(
    val firstPickerValue: Int,
    val secondPickerValue: Int?,
    val thirdPickerValue: Int?
) : Parcelable
