package com.websarva.wings.android.zuboradiary.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * 有効なUUID形式の文字列であることを保証する値クラス。
 *
 * このクラスは、必ず有効なUUID形式の文字列を保持する。
 *
 * @property value 有効なUUID形式の文字列。
 * @throws IllegalArgumentException 文字列が有効なUUID形式でない場合。
 */
@Parcelize // MEMO:"@Parcelize"でSavedStateHandle対応
@JvmInline
internal value class UUIDString(val value: String) : Parcelable {
    init {
        // 形式を検証
        UUID.fromString(value)
    }
}
