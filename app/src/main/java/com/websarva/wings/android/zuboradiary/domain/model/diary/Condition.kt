package com.websarva.wings.android.zuboradiary.domain.model.diary

import java.util.Arrays

/**
 * 日記に記録する体調の状態を表す enum クラス。
 *
 * 各 enum 定数は、整数値と対応付けて定義する。
 *
 * @property number 体調の状態を表す整数値。
 */
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class Condition @Suppress("unused") constructor(
    val number: Int
) {

    /** 不明な状態。 */
    UNKNOWN(0),

    /** 良い状態。 */
    @Suppress("unused") // 下記定数は直接使用されていないが必要な為、@Suppressで警告回避。
    HAPPY(1),

    /** やや良い状態。 */
    @Suppress("unused") // 同上
    GOOD(2),

    /** 普通の状態。 */
    @Suppress("unused") // 同上
    AVERAGE(3),

    /** やや悪い状態。 */
    @Suppress("unused") // 同上
    POOR(4),

    /** 悪い状態。 */
    @Suppress("unused") // 同上
    BAD(5);

    companion object {
        /**
         * 指定された整数値に対応する [Condition] を返す。
         *
         * @param number 検索する[Condition]の要素に対応付けされた整数値。
         * @return 対応する [Condition]。
         * @throws NoSuchElementException 指定された整数値に対応する [Condition] が見つからない場合。
         */
        fun of(number: Int): Condition {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Condition -> x.number == number }.findFirst().get()
        }
    }
}
