package com.websarva.wings.android.zuboradiary.domain.model

import java.util.Arrays

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class Condition @Suppress("unused") constructor(
    private val number: Int
) {

    UNKNOWN(0),
    @Suppress("unused") // 下記定数は直接使用されていないが必要な為、@Suppressで警告回避。
    HAPPY(1),
    @Suppress("unused") // 同上
    GOOD(2),
    @Suppress("unused") // 同上
    AVERAGE(3),
    @Suppress("unused") // 同上
    POOR(4),
    @Suppress("unused") // 同上
    BAD(5);

    companion object {
        fun of(number: Int): Condition {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Condition -> x.toNumber() == number }.findFirst().get()
        }
    }

    fun toNumber(): Int {
        return number
    }
}
