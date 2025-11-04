package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.domain.model.common.UUIDString
import java.util.UUID

/**
 * 日記の識別番号を表すデータクラス。
 *
 * このクラスは、必ず有効なUUID形式の文字列を保持する。
 *
 * @property value 日記の識別番号。
 * @throws IllegalArgumentException 文字列が有効なUUID形式でない場合。
 */
internal data class DiaryId(override val value: String) : UUIDString() {

    init {
        validate()
    }

    companion object {
        /**
         * 新しい [DiaryId]をランダムに生成する。
         */
        fun generate() = DiaryId(UUID.randomUUID().toString())
    }
}
