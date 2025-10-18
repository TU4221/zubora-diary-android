package com.websarva.wings.android.zuboradiary.domain.model.diary

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * 日記の項目のタイトルを表すバリュークラス。
 *
 * @property value 項目のタイトル。15文字以内でなければならない。
 * @throws IllegalArgumentException 文字列が15文字を超える場合。
 */
@JvmInline
@Serializable
internal value class DiaryItemTitle(val value: String) : JavaSerializable {
    init {
        require(value.length <= 15) {
            "項目のタイトルは15文字以内で入力。"
        }
    }

    companion object {
        /**
         * 空の [DiaryItemTitle]を生成する。
         */
        fun empty() = DiaryItemTitle("")
    }
}
