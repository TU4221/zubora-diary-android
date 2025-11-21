package com.websarva.wings.android.zuboradiary.ui.model.diary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Arrays

/**
 * 日記の天気を表すUIモデル。
 *
 * 各天気は、データベース保存用の数値[number]を持つ。
 * @property number 天気を表す数値。
 */
@Parcelize
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
enum class WeatherUi @Suppress("unused") constructor(
    val number: Int
) : Parcelable {

    UNKNOWN(0),
    SUNNY(1),
    CLOUDY(2),
    RAINY(3),
    SNOWY(4);

    companion object {
        fun of(number: Int): WeatherUi {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: WeatherUi -> x.number == number }.findFirst().get()
        }
    }
}
