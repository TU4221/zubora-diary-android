package com.websarva.wings.android.zuboradiary.domain.model.diary

/**
 * 日記の項目のタイトルを表すバリュークラス。
 *
 * @property value 項目のタイトル。20文字以内でなければならない。
 * @throws IllegalArgumentException 文字列が20文字を超える場合。
 */
@JvmInline
internal value class DiaryItemTitle(val value: String) {
    init {
        require(value.length <= 20) {
            "項目のタイトルは20文字以内で入力。"
        }
    }

    companion object {
        /**
         * 空の [DiaryItemTitle]を生成する。
         */
        fun empty() = DiaryItemTitle("")
    }
}
