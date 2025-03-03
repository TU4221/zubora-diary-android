package com.websarva.wings.android.zuboradiary.data.diary

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
enum class Condition @Suppress("unused") constructor(
    private val number: Int,
    private val stringResId: Int
) {

    UNKNOWN(0, R.string.enum_condition_unknown),
    @Suppress("unused") // 下記定数は直接使用されていないが必要な為、@Suppressで警告回避。
    HAPPY(1, R.string.enum_condition_happy),
    @Suppress("unused") // 同上
    GOOD(2, R.string.enum_condition_good),
    @Suppress("unused") // 同上
    AVERAGE(3, R.string.enum_condition_average),
    @Suppress("unused") // 同上
    POOR(4, R.string.enum_condition_poor),
    @Suppress("unused") // 同上
    BAD(5, R.string.enum_condition_bad);

    companion object {
        fun of(number: Int): Condition {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Condition -> x.toNumber() == number }.findFirst().get()
        }

        fun of(context: Context, strCondition: String): Condition {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Condition -> x.toString(context) == strCondition }.findFirst().get()
        }
    }

    fun toString(context: Context): String {
        return context.getString(stringResId)
    }

    fun toNumber(): Int {
        return number
    }
}
