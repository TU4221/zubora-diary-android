package com.websarva.wings.android.zuboradiary.domain.model

import java.util.Arrays

/**
 * 日記に記録する天気の状態を表す enum クラス。
 *
 * 各 enum 定数は、整数値と対応付けて定義する。
 *
 * @property number 天気の状態を表す整数値。
 */
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class Weather @Suppress("unused") constructor(
    private val number: Int
) {

    /** 不明な天気。 */
    UNKNOWN(0),

    /** 晴れ。 */
    SUNNY(1),

    /** 曇り。 */
    CLOUDY(2),

    /** 雨。 */
    RAINY(3),

    /** 雪。 */
    SNOWY(4);

    companion object {
        /**
         * 指定された整数値に対応する [Weather] を返す。
         *
         * @param number 検索する[Weather]の要素に対応付けされた整数値。
         * @return 対応する [Weather]。
         * @throws NoSuchElementException 指定された整数値に対応する [Weather] が見つからない場合。
         */
        fun of(number: Int): Weather {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Weather -> x.toNumber() == number }.findFirst().get()
        }
    }

    /**
     * この天気の状態を表す整数値を返す。
     *
     * @return 天気の整数値。
     */
    fun toNumber(): Int {
        return number
    }
}
