package com.websarva.wings.android.zuboradiary.domain.model.diary

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * 検索単語を表すバリュークラス。
 *
 * @property value 検索単語。
 * @throws IllegalArgumentException 検索単語の文字列が空の場合。
 */
@JvmInline
@Serializable
internal value class SearchWord(val value: String) : JavaSerializable {
    init {
        require(value.isNotEmpty()) { "検索単語が空の状態。" }
    }
}
