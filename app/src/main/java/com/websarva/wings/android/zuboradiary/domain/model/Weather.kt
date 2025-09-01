package com.websarva.wings.android.zuboradiary.domain.model

import java.util.Arrays

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class Weather @Suppress("unused") constructor(
    private val number: Int
) {

    UNKNOWN(0),
    SUNNY(1),
    CLOUDY(2),
    RAINY(3),
    SNOWY(4);

    companion object {
        fun of(number: Int): Weather {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Weather -> x.toNumber() == number }.findFirst().get()
        }
    }

    fun toNumber(): Int {
        return number
    }
}
