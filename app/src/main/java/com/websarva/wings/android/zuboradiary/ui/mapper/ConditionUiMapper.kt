package com.websarva.wings.android.zuboradiary.ui.mapper

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

// TODO:Spinnerを修正してから下記削除
/**
 * ローカライズされた文字列から対応するConditionUi enumを取得する。
 *
 * @param context 文字列リソース解決のためのContext。
 * @param strCondition 変換元の文字列。
 * @return 対応するConditionUi。見つからない場合は例外をスローする。
 */
internal fun conditionUiFromString(context: Context, strCondition: String): ConditionUi {
    return ConditionUi.entries.first { it.asString(context) == strCondition }
}
