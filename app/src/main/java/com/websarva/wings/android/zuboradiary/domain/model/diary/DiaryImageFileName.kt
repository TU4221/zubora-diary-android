package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.domain.model.common.FileName
import kotlinx.serialization.Serializable

/**
 * ファイル名を表すデータクラス。
 *
 * このクラスは、ファイル名が空ではなく、一般的ファイル形式 (basename.extension) であることを保障する。
 *
 * @property fullName ファイル名。拡張子を含む。
 * @throws IllegalArgumentException ファイル名が一般的ファイル形式 (basename.extension) でない場合。
 */
@Serializable
internal data class DiaryImageFileName(
    override val fullName: String
) : FileName() {

    init {
        validate()
    }
}
