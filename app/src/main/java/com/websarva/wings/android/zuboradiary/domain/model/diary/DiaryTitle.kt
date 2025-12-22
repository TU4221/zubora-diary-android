package com.websarva.wings.android.zuboradiary.domain.model.diary

/**
 * 日記のタイトルを表すバリュークラス。
 *
 * @property value 日記のタイトル。20文字以内でなければならない。
 * @throws IllegalArgumentException 文字列が20文字を超える場合。
 */
@JvmInline
internal value class DiaryTitle(val value: String) {
    init {
        require(value.length <= 20) {
            "日記のタイトルは20文字以内で入力。"
        }
    }
}
