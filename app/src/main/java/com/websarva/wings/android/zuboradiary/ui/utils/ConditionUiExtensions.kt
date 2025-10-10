package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi

/**
 * ConditionUi enumに対応する文字列リソースIDを取得する拡張プロパティ。
 */
internal val ConditionUi.stringResId: Int
    get() = when (this) {
        ConditionUi.UNKNOWN -> R.string.enum_condition_unknown
        ConditionUi.HAPPY -> R.string.enum_condition_happy
        ConditionUi.GOOD -> R.string.enum_condition_good
        ConditionUi.AVERAGE -> R.string.enum_condition_average
        ConditionUi.POOR -> R.string.enum_condition_poor
        ConditionUi.BAD -> R.string.enum_condition_bad
    }

/**
 * ConditionUiをContextを使ってローカライズされた文字列に変換する拡張関数。
 *
 * @param context 文字列リソース解決のためのContext。
 * @return 対応する文字列。
 */
internal fun ConditionUi.asString(context: Context): String {
    return context.getString(this.stringResId)
}
