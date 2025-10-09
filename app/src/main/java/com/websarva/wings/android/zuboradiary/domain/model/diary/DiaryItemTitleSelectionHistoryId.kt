package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.domain.model.common.UUIDString
import java.util.UUID

/**
 * 日記項目タイトル選択履歴の識別番号を表すデータクラス。
 *
 * このクラスは、必ず有効なUUID形式の文字列を保持する。
 *
 * @property value 有効なUUID形式の文字列。
 * @throws IllegalArgumentException 文字列が有効なUUID形式でない場合。
 */
internal data class DiaryItemTitleSelectionHistoryId(override val value: String) : UUIDString(value) {

    init {
        validate()
    }

    companion object {
        /**
         * 新しい [DiaryItemTitleSelectionHistoryId] をランダムに生成する。
         */
        fun generate() = DiaryItemTitleSelectionHistoryId(UUID.randomUUID().toString())
    }
}
