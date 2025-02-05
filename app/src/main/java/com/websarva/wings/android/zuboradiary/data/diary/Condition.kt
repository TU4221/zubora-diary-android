package com.websarva.wings.android.zuboradiary.data.diary

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

enum class Condition(private val number: Int, private val stringResId: Int) {
    UNKNOWN(0, R.string.enum_condition_unknown),
    HAPPY(1, R.string.enum_condition_happy),
    GOOD(2, R.string.enum_condition_good),
    AVERAGE(3, R.string.enum_condition_average),
    POOR(4, R.string.enum_condition_poor),
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
