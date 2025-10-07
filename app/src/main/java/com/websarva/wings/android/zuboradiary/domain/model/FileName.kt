package com.websarva.wings.android.zuboradiary.domain.model

/**
 * ファイル名を表すバリュークラス。
 *
 * このクラスは、ファイル名が空ではなく、一般的ファイル形式 (basename.extension) であることを保障する。
 *
 * @property fullName ファイル名。拡張子を含む。
 * @throws IllegalArgumentException ファイル名が一般的ファイル形式 (basename.extension) でない場合。
 */
@JvmInline
internal value class FileName(
    val fullName: String
) {

    init {
        // 1. 空でないこと
        require(fullName.isNotBlank()) { "ファイル名が空です。" }

        // 2. ベース名と拡張子の間の「.」が含まれていること
        val dotIndex = fullName.lastIndexOf('.')
        require(dotIndex != -1) { "ファイル名にベース名と拡張子を分ける `.` が含まれていません。" }

        // 3. ベース名と拡張子がふくまれていること(「.」の前後に文字が1文字以上含まれていること)
        require(dotIndex > 0) { "ベース名が含まれていません。" }
        require(dotIndex < fullName.length - 1) { "拡張子が含まれていません。" }
    }
}
