package com.websarva.wings.android.zuboradiary.domain.model.diary

/**
 * 日記のタイトルを表すバリュークラス。
 *
 * @property value 日記のタイトル。15文字以内でなければならない。
 * @throws IllegalArgumentException 文字列が15文字を超える場合。
 */
@JvmInline
internal value class DiaryTitle(val value: String) {
    init {
        require(value.length <= 15) {
            "日記のタイトルは15文字以内で入力。"
        }
    }
}
