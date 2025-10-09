package com.websarva.wings.android.zuboradiary.domain.model.diary

/**
 * 日記の項目のコメントを表すバリュークラス。
 *
 * @property value 項目のコメント。50文字以内でなければならない。
 * @throws IllegalArgumentException 文字列が50文字を超える場合。
 */
@JvmInline
internal value class DiaryItemComment(val value: String) {
    init {
        require(value.length <= 50) {
            "項目のコメントは50文字以内で入力してください。"
        }
    }

    companion object {
        /**
         * 空の [DiaryItemComment]を生成する。
         */
        fun empty() = DiaryItemComment("")
    }
}
