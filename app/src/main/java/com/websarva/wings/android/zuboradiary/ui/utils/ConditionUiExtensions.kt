package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi

/** [ConditionUi]に対応する文字列リソースIDを返す。 */
private val ConditionUi.stringResId: Int
    get() = when (this) {
        ConditionUi.UNKNOWN -> R.string.enum_condition_unknown
        ConditionUi.HAPPY -> R.string.enum_condition_happy
        ConditionUi.GOOD -> R.string.enum_condition_good
        ConditionUi.AVERAGE -> R.string.enum_condition_average
        ConditionUi.POOR -> R.string.enum_condition_poor
        ConditionUi.BAD -> R.string.enum_condition_bad
    }

/**
 * [ConditionUi]を、ユーザーに表示するための文字列に変換する。
 * @param context 文字列リソースを取得するためのコンテキスト。
 */
internal fun ConditionUi.asString(context: Context): String {
    return context.getString(this.stringResId)
}
