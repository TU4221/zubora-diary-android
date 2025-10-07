package com.websarva.wings.android.zuboradiary.domain.model

import java.util.UUID

/**
 * 日記の識別番号を表すバリュークラス。
 *
 * このクラスは、必ず有効なUUID形式の文字列を保持する。
 *
 * @property value 日記の識別番号。
 * @throws IllegalArgumentException 文字列が有効なUUID形式でない場合。
 */
@JvmInline
internal value class DiaryId(val value: String = UUID.randomUUID().toString()) {
    init {
        // 形式を検証
        UUID.fromString(value)
    }
}
